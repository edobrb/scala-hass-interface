package hass.controller

import java.util.concurrent.CompletableFuture

import com.github.andyglow.websocket.{Websocket, WebsocketClient}
import hass.model.common.Observable
import hass.model.event.{Event, StateChangedEvent}
import hass.model.service.{Result, Service}
import hass.model.state.EntityState
import hass.parser.{EventParser, ResultParser, StateParser}
import play.api.libs.json._
import utils.Logger.log

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success, Try}

class Hass(hassUrl: String) extends Observable[Event] {
  protected val client: WebsocketClient[String] = WebsocketClient[String](s"ws://$hassUrl/api/websocket") {
    case str =>
      val json = Json.parse(str)
      ((json \ "type").asOpt[String], (json \ "id").asOpt[Long]) match {
        case (Some("auth_required"), _) => //nothing

        case (Some("auth_invalid"), _) =>
          log wrn "Auth failed. Connection closed."
          close()

        case (Some("auth_ok"), _) =>
          ping()
          log inf "Auth ok. Subscribing to all events... Fetching all states..."
          val subscribeEventsId = nextId
          socket ! "{\"id\":" + subscribeEventsId + ",\"type\":\"subscribe_events\"}"
          val eventsFuture = new CompletableFuture[Result]
          pendingRequest += (subscribeEventsId -> eventsFuture)
          eventsFuture.thenAccept {
            case Result(true, _) => log inf "Subscribed to all events."
            case _ => log inf "Subscribed to all events failed."
          }

          val fetchStateId = nextId
          socket ! "{\"id\":" + fetchStateId + ",\"type\":\"get_states\"}"
          val future = new CompletableFuture[Result]
          pendingRequest += (fetchStateId -> future)
          future.thenAccept(result => entityStates.synchronized {
            result.result match {
              case Some(JsArray(v)) => v.foreach(s => {
                StateParser(s) match {
                  case Some(state) => entityStates += (state.entity_id -> state)
                  case None => log err ("parsing error: " + s)
                }
              })
              case _ => log err "unexpected result in get_states"
            }
            entityStates.notify()
            log inf "Fetched all states."
          })

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
  protected val socket: Websocket = client.open()
  private val nextIdLock = new Object()
  private val pendingRequest = scala.collection.mutable.Map[Long, CompletableFuture[Result]]()
  private val entityStates = scala.collection.mutable.Map[String, EntityState[_]]()
  private var nextIdVar: Long = 0

  def auth(token: String): Unit = {
    socket ! "{\"type\":\"auth\",\"access_token\":\"" + token + "\"}"
    entityStates.synchronized {
      entityStates.wait()
    }
  }

  onEvent {
    case StateChangedEvent(entity_id, _, newState, _, _) => entityStates.synchronized {
      entityStates(entity_id) = newState
    }
  }

  private def ping(): Unit = {
    ExecutionContext.global.execute(() => {
      Thread.sleep(1000)
      val pingFuture = send(id => "{\"id\":" + id + ",\"type\":\"ping\"}")
      Try(Await.result(pingFuture, 2.seconds)) match {
        case Failure(_) => println("Not received pong response in 2 seconds!")
        case Success(_) => ping()
      }
    })
  }

  def send(f: Long => String): scala.concurrent.Future[Result] = {
    import scala.compat.java8.FutureConverters._
    val future = new CompletableFuture[Result]
    val reqId = nextId
    val str = f(reqId)

    pendingRequest += reqId -> future
    socket ! str
    toScala(future)
  }

  def call(req: Service): scala.concurrent.Future[Result] =
    send(id => req.materialize(id).toString())

  private def nextId: Long = nextIdLock.synchronized {
    nextIdVar = nextIdVar + 1
    nextIdVar
  }

  def stateOf[E <: EntityState[_]](entity_id: String): Option[E] = entityStates.synchronized {
    if (entityStates.contains(entity_id)) {
      Some(entityStates(entity_id).asInstanceOf[E])
    } else {
      None
    }
  }

  def onEvent(f: PartialFunction[Event, Unit]): Unit = addObserver(f)

  def onStateChange(f: PartialFunction[EntityState[_], Unit]): Unit = addObserver({
    case StateChangedEvent(_, _, newState, _, _) if f.isDefinedAt(newState) => f(newState)
  })

  def close(): Unit = client.shutdownSync()
}
