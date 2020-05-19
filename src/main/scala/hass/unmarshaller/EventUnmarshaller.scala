package hass.unmarshaller

import com.github.nscala_time.time.Imports.DateTime
import hass.model.event.{Event, ServiceCallEvent, StateChangedEvent, UnknownEvent}
import hass.unmarshaller.CommonUnmarshaller._
import play.api.libs.json._

object EventUnmarshaller extends JsonUnmarshaller[Event] {

  override def apply(data: JsValue): Option[Event] = first(all)(data)

  def all: Seq[JsonUnmarshaller[Event]] = Seq[JsonUnmarshaller[Event]](
    stateChanged,
    serviceCall,
    unknown)

  def unknown: JsonUnmarshaller[UnknownEvent] = data =>
    for (event <- extractEvent(data);
         (origin, timeFired) <- originAndTimeFired(event))
      yield UnknownEvent(data, timeFired, origin)

  def serviceCall: JsonUnmarshaller[ServiceCallEvent] = data =>
    for (event <- extractEvent(data);
         "call_service" <- str("event_type")(event);
         (origin, timeFired) <- originAndTimeFired(event);
         eventData <- json("data")(event);
         service <- first(ServiceUnmarshaller.all)(eventData))
      yield ServiceCallEvent(service, timeFired, origin)

  def stateChanged: JsonUnmarshaller[StateChangedEvent[_]] = data =>
    for (event <- extractEvent(data);
         "state_changed" <- str("event_type")(event);
         (origin, timeFired) <- originAndTimeFired(event);
         eventData <- json("data")(event);
         entityId <- str("entity_id")(eventData);
         oldStateData <- json("old_state")(eventData);
         newStateData <- json("new_state")(eventData);
         oldState <- StateUnmarshaller(oldStateData);
         newState <- StateUnmarshaller(newStateData))
      yield StateChangedEvent(entityId, oldState, newState, timeFired, origin)

  def extractEvent: JsonUnmarshaller[JsValue] = data =>
    for ("event" <- str("type")(data);
         event <- json("event")(data)) yield event

  def originAndTimeFired: JsonUnmarshaller[(String, DateTime)] = data =>
    for (origin <- str("origin")(data);
         timeFired <- datetime("time_fired")(data))
      yield (origin, timeFired)
}