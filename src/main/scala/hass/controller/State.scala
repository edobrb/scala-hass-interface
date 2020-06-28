package hass.controller

import hass.controller.State._
import hass.model.event.{ConnectionReadyEvent, Event, StateChangedEvent}
import hass.model.service.Result
import hass.model.state.EntityState
import hass.unmarshaller.{EventUnmarshaller, ResultUnmarshaller, StateUnmarshaller}
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsValue, Json}
import utils.Logger

import scala.util.{Failure, Success, Try}

object State {
  type ID = Long
  type EntityId = String
  type Request = String
  type Message = String

  def empty(authToken: String, log: Logger): State = State(Map(), 1, Seq(), Seq(), Map(), authToken, log)
}

case class State(entitiesStates: Map[EntityId, EntityState[_]],
                 nextId: ID,
                 pendingOutputs: Seq[Message],
                 pendingEvents: Seq[Event],
                 pendingRequest: Map[ID, (State, Result) => State],
                 authToken: String,
                 log: Logger) {

  def addPendingEventNotification(event: Event): State = copy(pendingEvents = pendingEvents :+ event)

  def extractPendingEvents(): (Seq[Event], State) = (pendingEvents, copy(pendingEvents = Seq()))

  def addPendingOutput(message: Message): State = copy(pendingOutputs = pendingOutputs :+ message)

  def extractPendingOutputs(): (Seq[Message], State) = (pendingOutputs, copy(pendingOutputs = Seq()))

  def addPendingRequest(id: ID, t: (State, Result) => State): State =
    copy(pendingRequest = pendingRequest + (id -> t))

  def updateEntitiesStates(id: EntityId, state: EntityState[_]): State =
    copy(entitiesStates = entitiesStates + (id -> state))

  def completePendingRequest(id: ID, result: Result): State =
    pendingRequest(id)(this, result).copy(pendingRequest = pendingRequest - id)

  def generateRequest(r: ID => Message, t: (State, Result) => State): State =
    copy(nextId = nextId + 1).addPendingOutput(r(nextId)).addPendingRequest(nextId, t)

  private def digestAuthRequired(): State = {
    log inf "Authentication required, providing jwt..."
    addPendingOutput("{\"type\":\"auth\",\"access_token\":\"" + authToken + "\"}")
  }

  private def digestAuthInvalid(): State = {
    log wrn "Authentication invalid."
    this
  }

  private def digestAuthOk(): State = {
    log inf "Auth ok. Subscribing to all events... Fetching all states..."
    generateRequest(id => "{\"id\":" + id + ",\"type\":\"subscribe_events\"}",
      {
        case (state, Result(true, _)) => log inf "Subscribed to all events."; state
        case (state, _) => log inf "Subscribed to all events failed."; state
      }).generateRequest(id => "{\"id\":" + id + ",\"type\":\"get_states\"}",
      {
        case (state, Result(true, Some(JsArray(entityStates)))) =>
          val newState = entityStates.foldLeft(state) {
            case (state, entityState) => StateUnmarshaller(entityState) match {
              case Some(value) =>
                state
                  .addPendingEventNotification(StateChangedEvent(value.entityId, value, value, DateTime.now(), "INTERNAL"))
                  .updateEntitiesStates(value.entityId, value)
              case None => log err ("parsing error: " + entityState); state
            }
          }
          log inf "Fetched all states."
          newState
        case (state, result) =>
          log err ("Failed to fetch states. Invalid result: " + result)
          state
      }).addPendingEventNotification(ConnectionReadyEvent)
  }

  private def digestResult(id: ID, json: JsValue): State =
    completePendingRequest(id, ResultUnmarshaller(json).getOrElse(Result.parsingError))


  private def digestEvent(json: JsValue): State =
    EventUnmarshaller(json) match {
      case Some(event) => addPendingEventNotification(event)
      case None => log err "Malformed event: " + json; this
    }

  private def digestPong(id: ID): State =
    completePendingRequest(id, Result(success = true, None))

  private def defaultDigest(json: JsValue): State = {
    log err "Unknown message: " + json
    this
  }

  def digest(msg: Message): State = {
    val maybeJson = Try(Json.parse(msg))
    maybeJson match {
      case Failure(_) =>
        log err "Received invalid message: " + msg
        this
      case Success(json) => ((json \ "type").asOpt[String], (json \ "id").asOpt[Long]) match {
        case (Some("auth_required"), _) => digestAuthRequired()
        case (Some("auth_invalid"), _) => digestAuthInvalid()
        case (Some("auth_ok"), _) => digestAuthOk()
        case (Some("result"), Some(id)) => digestResult(id, json)
        case (Some("event"), _) => digestEvent(json)
        case (Some("pong"), Some(id)) => digestPong(id)
        case _ => defaultDigest(json)
      }
    }
  }

  def clear: State = State.empty(authToken, log)
}

