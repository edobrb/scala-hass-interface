package hass.parser

import hass.model.state.LightState
import hass.model.state.ground.On
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest._
import play.api.libs.json.Json

class StateParserTest extends FunSuite {

  private val lightState1 = Json.parse("{\"entity_id\":\"light.bed_light\",\"last_changed\":\"2015-11-26T01:37:24.265390+00:00\",\"state\":\"on\",\"attributes\":{\"rgb_color\":[254,208,0],\"color_temp\":380,\"supported_features\":147,\"xy_color\":[0.5,0.5],\"brightness\":180,\"white_value\":200,\"friendly_name\":\"Bed Light\"},\"last_updated\":\"2016-11-26T01:37:24.265390+00:00\"}")

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
        assert(s.lastUpdated.getMillis - new DateTime(2016, 11, 26, 1, 37, 24, DateTimeZone.UTC).getMillis < 1000)
      case _ => fail()
    }
  }
}
