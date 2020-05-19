package hass.controller

import java.util.concurrent.CompletableFuture

import hass.model.common.Observable
import hass.model.event.{ConnectionClosedEvent, ConnectionOpenEvent, Event, StateChangedEvent}
import hass.model.service.{Result, Service}
import hass.model.state.EntityState
import hass.unmarshaller.{EventUnmarshaller, ResultUnmarshaller, StateUnmarshaller}
import org.joda.time.DateTime
import play.api.libs.json._
import utils.{ConsoleLogger, IdDispatcher, Logger}

import scala.compat.java8.FutureConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

object Hass {
  def apply(hassUrl: String, token: String, retryOnError: Boolean, logger: Logger = ConsoleLogger): Hass =
    new Hass(hassUrl, token, retryOnError, logger)
}

class Hass(hassUrl: String, token: String, retryOnError: Boolean, log: Logger) extends Observable[Event] {

  private val pendingRequest = scala.collection.mutable.Map[Long, CompletableFuture[Result]]()
  private val entityStates = scala.collection.mutable.Map[String, EntityState[_]]()
  private val ids: IdDispatcher = IdDispatcher(1)
  private var socket: Option[WebsocketIO] = None

  onEvent {
    case StateChangedEvent(entity_id, _, newState, _, _) => entityStates.synchronized {
      entityStates(entity_id) = newState
    }
  }

  connect()

  def call(req: Service): scala.concurrent.Future[Result] =
    send(id => {
      val res = req.materialize(id).toString()
      res
    })

  def stateOf[E <: EntityState[_]](entityId: String): Option[E] = entityStates.synchronized {
    if (entityStates.contains(entityId)) {
      Some(entityStates(entityId).asInstanceOf[E])
    } else {
      None
    }
  }

  def onEvent(f: PartialFunction[Event, Unit]): Unit = addObserver(f)

  def onStateChange(f: PartialFunction[EntityState[_], Unit]): Unit = onEvent({
    case StateChangedEvent(_, _, newState, _, _) if f.isDefinedAt(newState) => f(newState)
  })

  def onConnection(f: () => Unit): Unit = onEvent({
    case ConnectionOpenEvent => f()
  })

  def onClose(f: () => Unit): Unit = onEvent({
    case ConnectionClosedEvent => f()
  })

  def close(): Unit = socket match {
    case Some(s) =>
      log inf "Closing..."
      s.closeAsync().onComplete(_ => {
        log inf "Closed."
      })(ExecutionContext.global)
      socket = None
    case None =>
  }

  private def connect(): Unit = {
    ExecutionContext.global.execute(() => {
      socket match {
        case Some(s) => log inf "Closing..."
          s.close()
          socket = None
          log inf "Closed."
        case _ =>
      }
      log inf "Connecting..."
      WebsocketIO(s"ws://$hassUrl/api/websocket", messageHandler, exceptionHandler) match {
        case Some(s) =>
          log inf "Connected."
          socket = Some(s)
          s send "{\"type\":\"auth\",\"access_token\":\"" + token + "\"}"
          notifyObservers(ConnectionOpenEvent)
        case None =>
          log err "Error while opening connection."
          notifyObservers(ConnectionClosedEvent)
          if (retryOnError) {
            connect()
          } else {
            close()
          }
      }
    })
  }

  private def send(f: Long => String): scala.concurrent.Future[Result] = {
    socket match {
      case Some(s) =>
        val future = new CompletableFuture[Result]
        val reqId = ids.next
        val str = f(reqId)
        pendingRequest += reqId -> future
        s send str
        toScala(future)
      case None =>
        val future = new CompletableFuture[Result]
        future.complete(Result(success = false, None))
        toScala(future)
    }
  }

  private def ping(): Unit = {
    ExecutionContext.global.execute(() => {
      Thread.sleep(100)
      val pingFuture = send(id => "{\"id\":" + id + ",\"type\":\"ping\"}")
      Try(Await.result(pingFuture, 2.seconds)) match {
        case Failure(_) => log err "Not received pong response in 2 seconds!"
          if (retryOnError) connect()
          else close()
        case Success(_) => ping()
      }
    })
  }

  private def messageHandler(msg: String): Unit = {
    val json = Json.parse(msg)
    ((json \ "type").asOpt[String], (json \ "id").asOpt[Long]) match {
      case (Some("auth_required"), _) => //nothing

      case (Some("auth_invalid"), _) =>
        log wrn "Auth failed. Connection closed."
        if (retryOnError) {
          connect()
        } else {
          this.close()
        }

      case (Some("auth_ok"), _) => socket match {
        case Some(s) => ping()
          log inf "Auth ok. Subscribing to all events... Fetching all states..."
          val subscribeEventsId = ids.next
          s send "{\"id\":" + subscribeEventsId + ",\"type\":\"subscribe_events\"}"
          val eventsFuture = new CompletableFuture[Result]
          pendingRequest += (subscribeEventsId -> eventsFuture)
          eventsFuture.thenAccept {
            case Result(true, _) => log inf "Subscribed to all events."
            case _ => log inf "Subscribed to all events failed."
          }

          val fetchStateId = ids.next
          s send "{\"id\":" + fetchStateId + ",\"type\":\"get_states\"}"
          val future = new CompletableFuture[Result]
          pendingRequest += (fetchStateId -> future)
          future.thenAccept(result => entityStates.synchronized {
            result.result match {
              case Some(JsArray(v)) => v.foreach(s => {
                StateUnmarshaller(s) match {
                  case Some(state) =>
                    entityStates += (state.entityId -> state)
                    //TODO: not very correct
                    notifyObservers(StateChangedEvent(state.entityId, state, state, DateTime.now(), "INTERNAL"))
                  case None => log err ("parsing error: " + s)
                }
              })
              case _ => log err "unexpected result in get_states"
            }
            log inf "Fetched all states."

          })
        case None => log err "Received auth_ok but socket is closed"
      }


      case (Some("result"), Some(id)) =>
        if (pendingRequest.isDefinedAt(id)) {
          pendingRequest(id).complete(ResultUnmarshaller(json).getOrElse(Result.parsingError))
          pendingRequest -= id
        } else {
          log err "Malformed result: " + json
        }


      case (Some("event"), _) =>
        EventUnmarshaller(json) match {
          case Some(event) => notifyObservers(event)
          case None => log err "Malformed event: " + json
        }

      case (Some("pong"), Some(id)) =>
        if (pendingRequest.isDefinedAt(id.intValue)) {
          pendingRequest(id.intValue).complete(Result(success = true, None))
          pendingRequest -= id.intValue
        } else {
          log err "Malformed pong: " + json
        }

      case _ => log wrn "Unknown json: " + json
    }
  }

  private def exceptionHandler(exception: Throwable): Unit = {
    log err exception.getMessage
    notifyObservers(ConnectionClosedEvent)
  }
}
