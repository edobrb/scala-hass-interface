package hass.parser

import hass.model.state.{LightState, SwitchState, UnknownEntityState}
import hass.model.state.ground.{Off, On, Unavailable}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class StateParserTest extends FunSuite {

  val lightState1: JsValue = Json.parse("{\"entity_id\":\"light.bed_light\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"on\",\"attributes\":{\"rgb_color\":[254,208,0],\"color_temp\":380,\"supported_features\":147,\"xy_color\":[0.5,0.5],\"brightness\":180,\"white_value\":200,\"friendly_name\":\"Bed Light\"},\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val lightState2: JsValue = Json.parse("{\"entity_id\":\"light.bed_light\",\"last_changed\":\"2016-11-26T01:37:24.265390+00:00\",\"state\":\"on\",\"attributes\":{\"color_temp\":380,\"supported_features\":147,\"hs_color\":[0.5,0.5],\"brightness\":180,\"white_value\":200,\"friendly_name\":\"Bed Light\"},\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val switchState1: JsValue = Json.parse("{\"entity_id\":\"switch.garage_switch\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"off\",\"attributes\":{\"friendly_name\":\"Garage Switch\"},\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val switchState2: JsValue = Json.parse("{\"entity_id\":\"switch.garage_switch\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"unavailable\",\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val unknownState1: JsValue = Json.parse("{\"entity_id\":\"some_domain.some_name\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"unavailable\",\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val wrongState1: JsValue = Json.parse("{\"entity_id\":\"switch.garage_switch\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val wrongState2: JsValue = Json.parse("{\"entity_id\":\"switch.garage_switch\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"unavailable\"}")
  val wrongState3: JsValue = Json.parse("{\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"unavailable\",\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  val wrongState4: JsValue = Json.parse("{\"entity_id\":\"switch_garage_switch\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"unavailable\",\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")
  test("Parse light state 1") {
    StateParser(lightState1) match {
      case Some(s: LightState) =>
        assert(s.entity_name == "bed_light")
        assert(s.lastChanged.getMillis - new DateTime(2015, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
        assert(s.state == On)
        assert(s.rgb.exists { case (254, 208, 0) => true; case _ => false })
        assert(s.colorTemp.exists { case 380 => true; case _ => false })
        assert(s.attribute[Int]("supported_features").exists { case 147 => true; case _ => false })
        assert(s.xy.exists { case (0.5, 0.5) => true; case _ => false })
        assert(s.brightness.exists { case 180 => true; case _ => false })
        assert(s.white.exists { case 200 => true; case _ => false })
        assert(s.friendlyName.exists { case "Bed Light" => true; case _ => false })
        assert(s.transition.isEmpty)
        assert(s.profile.isEmpty)
        assert(s.kelvin.isEmpty)
        assert(s.color.isEmpty)
        assert(s.brightnessPct.isEmpty)
        assert(s.brightnessStep.isEmpty)
        assert(s.brightnessStepPct.isEmpty)
        assert(s.flash.isEmpty)
        assert(s.effect.isEmpty)
        assert(s.hs.isEmpty)
        assert(s.lastUpdated.getMillis - new DateTime(2016, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
      case _ => fail()
    }
  }
  test("Parse light state 2") {
    StateParser(lightState2) match {
      case Some(s: LightState) =>
        assert(s.rgb.isEmpty)
        assert(s.xy.isEmpty)
        assert(s.hs.exists { case (0.5, 0.5) => true; case _ => false })
      case _ => fail()
    }
  }
  test("Parse switch state 1") {
    StateParser(switchState1) match {
      case Some(s: SwitchState) =>
        assert(s.entity_name == "garage_switch")
        assert(s.lastChanged.getMillis - new DateTime(2015, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
        assert(s.state == Off)
        assert(s.attribute[Int]("some_random_attribute").isEmpty)
        assert(s.friendlyName.exists { case "Garage Switch" => true; case _ => false })
        assert(s.lastUpdated.getMillis - new DateTime(2016, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
      case _ => fail()
    }
  }
  test("Parse switch state 2") {
    StateParser(switchState2) match {
      case Some(s: SwitchState) => assert(s.state == Unavailable)
      case _ => fail()
    }
  }
  test("Parse unknown state 1") {
    StateParser(unknownState1) match {
      case Some(s: UnknownEntityState) =>
        assert(s.entity_name == "some_name")
        assert(s.entity_id == "some_domain.some_name")
        assert(s.lastChanged.getMillis - new DateTime(2015, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
        assert(s.state == "unavailable")
        assert(s.attribute[Int]("some_random_attribute").isEmpty)
        assert(s.lastUpdated.getMillis - new DateTime(2016, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
      case _ => fail()
    }
  }
  test("Parse wrong states") {
    assert(StateParser(wrongState1).isEmpty)
    assert(StateParser(wrongState2).isEmpty)
    assert(StateParser(wrongState3).isEmpty)
    assert(StateParser(wrongState4).isEmpty)
  }
}
