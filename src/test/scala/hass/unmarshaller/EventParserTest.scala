package hass.unmarshaller

import hass.model.event.{ServiceCallEvent, StateChangedEvent}
import hass.model.service.LightTurnService
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class EventParserTest extends FunSuite {
  val event1: JsValue = Json.parse("{\"id\":18,\"type\":\"event\",\"event\":{\"data\":{\"entity_id\":\"light.bed_light\",\"new_state\":{\"entity_id\":\"light.bed_light\",\"last_changed\":\"2016-11-26T01:37:24.265390+00:00\",\"state\":\"on\",\"attributes\":{\"rgb_color\":[254,208,0],\"color_temp\":380,\"supported_features\":147,\"xy_color\":[0.5,0.5],\"brightness\":180,\"white_value\":200,\"friendly_name\":\"Bed Light\"},\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"},\"old_state\":{\"entity_id\":\"light.bed_light\",\"last_changed\":\"2016-11-26T01:37:10.466994+00:00\",\"state\":\"off\",\"attributes\":{\"supported_features\":147,\"friendly_name\":\"Bed Light\"},\"last_updated\":\"2016-11-26T01:37:10.466994+00:00\"}},\"event_type\":\"state_changed\",\"time_fired\":\"2016-11-26T01:37:24.265429+00:00\",\"origin\":\"LOCAL\"}}")
  val event2: JsValue = Json.parse("{\"id\":18,\"type\":\"event\",\"event\":{\"data\":{\"domain\":\"light\",\"service\":\"turn_on\",\"service_data\":{\"entity_id\":\"light.kitchen\"}},\"event_type\":\"call_service\",\"time_fired\":\"2016-11-26T01:37:24.265429+00:00\",\"origin\":\"LOCAL\"}}")
  test("Parse event 1") {
    EventUnmarshaller(event1) match {
      case Some(res:StateChangedEvent[_]) =>
        assert(res.origin == "LOCAL")
        assert(res.timeFired.getMillis - new DateTime(2016, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
        assert(res.entityId == "light.bed_light")
      case _ => fail()
    }
  }
  test("Parse event 2") {
    EventUnmarshaller(event2) match {
      case Some(ServiceCallEvent(service:LightTurnService, timeFired, origin)) =>
        assert(origin == "LOCAL")
        assert(timeFired.getMillis - new DateTime(2016, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
        assert(service.entityNames.contains("kitchen"))
      case _ => fail()
    }
  }

}
