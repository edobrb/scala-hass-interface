package hass.controller

import java.util.concurrent.{CompletableFuture, Future}

import com.github.andyglow.websocket.{Websocket, WebsocketClient}
import hass.model.common.Observable
import hass.model.event.{Event, StateChangedEvent}
import hass.model.service.Service
import hass.model.service.result.{Result, ServiceCallResult}
import hass.model.state.EntityState
import hass.parser.{EventParser, StateParser}
import play.api.libs.json._
import utils.Logger.log

class Hass(hassUrl: String) extends Observable[Event] {
  protected val client: WebsocketClient[String] = WebsocketClient[String](s"ws://$hassUrl/api/websocket") {
    case str =>
      val json = Json.parse(str)
      json \ "type" match {
        case JsDefined(JsString("auth_required")) => //nothing

        case JsDefined(JsString("auth_invalid")) =>
          log wrn "Auth failed. Connection closed."
          close()

        case JsDefined(JsString("auth_ok")) =>
          log inf "Auth ok. Subscribing to all events... Fetching all states..."
          val subscribeEventsId = nextId
          socket ! "{\"id\":" + subscribeEventsId + ",\"type\":\"subscribe_events\"}"
          val eventsFuture = new CompletableFuture[JsValue]
          pendingRequest += (subscribeEventsId -> eventsFuture)
          eventsFuture.thenAccept(json => {
            log inf "Subscribed to all events."
          })


          val fetchStateId = nextId
          socket ! "{\"id\":" + fetchStateId + ",\"type\":\"get_states\"}"
          val future = new CompletableFuture[JsValue]
          pendingRequest += (fetchStateId -> future)
          future.thenAccept(json => entityStates.synchronized {
            json \ "result" match {
              case JsDefined(JsArray(v)) => v.foreach(s => {
                StateParser.parse(s) match {
                  case Some(state) => entityStates += (state.entity_id -> state)
                  case None => log err ("parsing error: " + s)
                }
              })
            }
            entityStates.notify()
            log inf "Fetched all states."
          })

        case JsDefined(JsString("result")) =>
          json \ "id" match {
            case JsDefined(JsNumber(id)) =>
              if (pendingRequest.isDefinedAt(id.intValue)) {
                pendingRequest(id.intValue).complete(json)
                pendingRequest -= id.intValue
              }
          }

        case JsDefined(JsString("event")) =>
          EventParser.parse(json) match {
            case Some(event) => notifyObservers(event)
            case None => log err "Malformed event: " + json
          }


        case _ => log wrn "Unknown json: " + json
      }

  }
  protected val socket: Websocket = client.open()
  private val nextIdLock = new Object()
  private val pendingRequest = scala.collection.mutable.Map[Long, CompletableFuture[JsValue]]()
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

  def call(req: Service): Result = (this callAsync req).get

  def callAsync(req: Service): Future[Result] = {
    val future = new CompletableFuture[JsValue]
    val reqId = nextId
    val reqJson: JsObject = req.materialize(reqId)

    pendingRequest += reqId -> future
    socket ! reqJson.toString()

    future.thenApply(json => {
      json \ "success" match {
        case JsDefined(JsBoolean(v)) => ServiceCallResult(v, json)
        case _ => ServiceCallResult(success = false, json)
      }
    })
  }

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

  def close(): Unit = client.shutdownSync()
}
