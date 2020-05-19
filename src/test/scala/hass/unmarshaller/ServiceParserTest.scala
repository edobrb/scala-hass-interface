package hass.unmarshaller

import hass.model.service.{LightTurnService, SwitchTurnService}
import hass.model.state.ground.Off
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class ServiceParserTest extends FunSuite {
  val service1: JsValue = Json.parse("{\"domain\":\"switch\",\"service\":\"turn_off\",\"service_data\":{\"entity_id\":[\"switch.kitchen\",\"switch.bed\"]}}")
  test("Parse result 1") {
    ServiceUnmarshaller(service1) match {
      case Some(SwitchTurnService(ids, Off)) =>
        assert(ids.contains("kitchen"))
        assert(ids.contains("bed"))
      case _ => fail()
    }
  }

}
