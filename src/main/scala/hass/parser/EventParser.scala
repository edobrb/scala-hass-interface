package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.event.{Event, ServiceCallEvent, StateChangedEvent, UnknownEvent}
import hass.parser.CommonParser._
import play.api.libs.json._

object EventParser {

  def parsers: Seq[JsValue => Option[Event]] = Seq(stateChangedEventParser, serviceCallEventParser, unknownEventParser)

  def event(implicit data: JsValue): Option[JsValue] =
    for("event" <- str("type");
        data <- json("event")) yield data

  def originAndTimeFired(implicit data: JsValue): Option[(String, DateTime)] =
    for (origin <- str("origin");
         timeFired <- datetime("time_fired"))
      yield (origin, timeFired)

  def unknownEventParser(data: JsValue): Option[Event] =
    for (event <- event(data);
         (origin, timeFired) <- originAndTimeFired(event))
      yield UnknownEvent(data, timeFired, origin)

  def serviceCallEventParser(data: JsValue): Option[Event] =
    for (event <- event(data);
         "call_service" <- str("event_type")(event);
         (origin, timeFired) <- originAndTimeFired(event);
         eventData <- json("data")(event);
         service <- first(ServiceParser.parsers)(eventData))
      yield ServiceCallEvent(service, timeFired, origin)

  def stateChangedEventParser(data: JsValue): Option[StateChangedEvent[_]] = {
    for (event <- event(data);
         "state_changed" <- str("event_type")(event);
         (origin, timeFired) <- originAndTimeFired(event);
         eventData <- json("data")(event);
         entityId <- str("entity_id")(eventData);
         oldStateData <- json("old_state")(eventData);
         newStateData <- json("new_state")(eventData);
         oldState <- StateParser.parse(oldStateData);
         newState <- StateParser.parse(newStateData))
      yield StateChangedEvent(entityId, oldState, newState, timeFired, origin)
  }

  def parse(data: JsValue): Option[Event] = first(parsers)(data)
}