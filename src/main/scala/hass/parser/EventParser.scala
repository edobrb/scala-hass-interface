package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.event.{Event, ServiceCallEvent, StateChangedEvent, UnknownEvent}
import hass.parser.CommonParser._
import play.api.libs.json._

object EventParser extends JsonParser[Event] {

  override def apply(data: JsValue): Option[Event] = first(all)(data)

  def all: Seq[JsonParser[Event]] = Seq[JsonParser[Event]](
    stateChanged,
    serviceCall,
    unknown)

  def unknown: JsonParser[UnknownEvent] = data =>
    for (event <- extractEvent(data);
         (origin, timeFired) <- originAndTimeFired(event))
      yield UnknownEvent(data, timeFired, origin)

  def serviceCall: JsonParser[ServiceCallEvent] = data =>
    for (event <- extractEvent(data);
         "call_service" <- str("event_type")(event);
         (origin, timeFired) <- originAndTimeFired(event);
         eventData <- json("data")(event);
         service <- first(ServiceParser.all)(eventData))
      yield ServiceCallEvent(service, timeFired, origin)

  def stateChanged: JsonParser[StateChangedEvent[_]] = data =>
    for (event <- extractEvent(data);
         "state_changed" <- str("event_type")(event);
         (origin, timeFired) <- originAndTimeFired(event);
         eventData <- json("data")(event);
         entityId <- str("entity_id")(eventData);
         oldStateData <- json("old_state")(eventData);
         newStateData <- json("new_state")(eventData);
         oldState <- StateParser(oldStateData);
         newState <- StateParser(newStateData))
      yield StateChangedEvent(entityId, oldState, newState, timeFired, origin)

  def extractEvent: JsonParser[JsValue] = data =>
    for ("event" <- str("type")(data);
         event <- json("event")(data)) yield event

  def originAndTimeFired: JsonParser[(String, DateTime)] = data =>
    for (origin <- str("origin")(data);
         timeFired <- datetime("time_fired")(data))
      yield (origin, timeFired)
}