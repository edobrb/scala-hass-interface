package hass.controller

import java.util.concurrent.CompletableFuture

import com.github.andyglow.websocket.{Websocket, WebsocketClient}
import hass.model.common.Observable
import hass.model.event.{ConnectionClosedEvent, ConnectionOpenEvent, Event, StateChangedEvent}
import hass.model.service.{Result, Service}
import hass.model.state.EntityState
import hass.parser.{EventParser, ResultParser, StateParser}
import org.joda.time.DateTime
import play.api.libs.json._
import utils.Logger.log

import scala.compat.java8.FutureConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

object Hass {
  def apply(hassUrl: String, token: String, retryOnError: Boolean): Hass =
    new Hass(hassUrl, token, retryOnError)
}

class Hass(hassUrl: String, token: String, retryOnError: Boolean) extends Observable[Event] {

  private val receiver: PartialFunction[String, Unit] = {
    case str =>
      val json = Json.parse(str)
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
            val subscribeEventsId = nextId
            s ! "{\"id\":" + subscribeEventsId + ",\"type\":\"subscribe_events\"}"
            val eventsFuture = new CompletableFuture[Result]
            pendingRequest += (subscribeEventsId -> eventsFuture)
            eventsFuture.thenAccept {
              case Result(true, _) => log inf "Subscribed to all events."
              case _ => log inf "Subscribed to all events failed."
            }

            val fetchStateId = nextId
            s ! "{\"id\":" + fetchStateId + ",\"type\":\"get_states\"}"
            val future = new CompletableFuture[Result]
            pendingRequest += (fetchStateId -> future)
            future.thenAccept(result => entityStates.synchronized {
              result.result match {
                case Some(JsArray(v)) => v.foreach(s => {
                  StateParser(s) match {
                    case Some(state) =>
                      entityStates += (state.entity_id -> state)
                      //TODO: not very correct
                      notifyObservers(StateChangedEvent(state.entity_id, state, state, DateTime.now(), "INTERNAL"))
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
            pendingRequest(id).complete(ResultParser(json).getOrElse(Result.parsingError))
            pendingRequest -= id
          } else {
            log err "Malformed result: " + json
          }


        case (Some("event"), _) =>
          EventParser(json) match {
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
  private val nextIdLock = new Object()
  private val pendingRequest = scala.collection.mutable.Map[Long, CompletableFuture[Result]]()
  private val entityStates = scala.collection.mutable.Map[String, EntityState[_]]()
  private var client: Option[WebsocketClient[String]] = None
  private var socket: Option[Websocket] = None
  private var nextIdVar: Long = 0

  connect()

  onEvent {
    case StateChangedEvent(entity_id, _, newState, _, _) => entityStates.synchronized {
      entityStates(entity_id) = newState
    }
  }

  def call(req: Service): scala.concurrent.Future[Result] =
    send(id => req.materialize(id).toString())

  private def send(f: Long => String): scala.concurrent.Future[Result] = {
    socket match {
      case Some(value) =>
        val future = new CompletableFuture[Result]
        val reqId = nextId
        val str = f(reqId)
        pendingRequest += reqId -> future
        value ! str
        toScala(future)
      case None =>
        val future = new CompletableFuture[Result]
        future.complete(Result(success = false, None))
        toScala(future)
    }
  }

  private def nextId: Long = nextIdLock.synchronized {
    nextIdVar = nextIdVar + 1
    nextIdVar
  }

  def stateOf[E <: EntityState[_]](entityId: String): Option[E] = entityStates.synchronized {
    if (entityStates.contains(entityId)) {
      Some(entityStates(entityId).asInstanceOf[E])
    } else {
      None
    }
  }

  def onEvent(f: PartialFunction[Event, Unit]): Unit = addObserver(f)

  def onStateChange(f: PartialFunction[EntityState[_], Unit]): Unit = addObserver({
    case StateChangedEvent(_, _, newState, _, _) if f.isDefinedAt(newState) => f(newState)
  })

  def onConnection(f: () => Unit): Unit = addObserver({
    case ConnectionOpenEvent => f()
  })

  def onClose(f: () => Unit): Unit = addObserver({
    case ConnectionClosedEvent => f()
  })

  def close(): Unit = client match {
    case Some(value) =>
      log inf "Closing..."
      value.shutdownAsync(ExecutionContext.global).onComplete(_ => {
        log inf "Closed."
      })(ExecutionContext.global)
      client = None
      socket = None
    case None =>
  }

  private def connect(): Unit = {
    ExecutionContext.global.execute(() => {
      client match {
        case Some(value) =>
          client = None
          socket = None
          log inf "Closing..."
          value.shutdownSync()
          log inf "Closed..."
        case None =>
      }
      log inf "Connecting..."
      val clientBuilder = WebsocketClient.Builder.apply[String](s"ws://$hassUrl/api/websocket")(receiver).onFailure({
        case exception =>
          log err exception.getMessage
          notifyObservers(ConnectionClosedEvent)
          if (retryOnError) {
            connect()
          } else {
            this.close()
          }
      })
      client = Some(clientBuilder.build())
      Try(client.get.open()) match {
        case Failure(exception) =>
          log err "While opening connection: " + exception.getMessage
          notifyObservers(ConnectionClosedEvent)
          if (retryOnError) {
            connect()
          } else {
            this.close()
          }
        case Success(openSocket) =>
          log inf "Connected."
          socket = Some(openSocket)
          openSocket ! "{\"type\":\"auth\",\"access_token\":\"" + token + "\"}"
          notifyObservers(ConnectionOpenEvent)
      }
    })
  }

  private def ping(): Unit = {
    ExecutionContext.global.execute(() => {
      Thread.sleep(1000)
      val pingFuture = send(id => "{\"id\":" + id + ",\"type\":\"ping\"}")
      Try(Await.result(pingFuture, 2.seconds)) match {
        case Failure(_) => log err "Not received pong response in 2 seconds!"
          if (retryOnError) connect()
          else close()
        case Success(_) => ping()
      }
    })
  }
}
