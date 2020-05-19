package hass.unmarshaller

import org.scalatest._
import play.api.libs.json.{JsValue, Json}

class ResultParserTest extends FunSuite {
  val result1: JsValue = Json.parse("{\"id\":18,\"type\":\"result\",\"success\":true,\"result\":3}")
  val wrongResult1: JsValue = Json.parse("{\"id\":18,\"type\":\"results\",\"success\":true,\"result\":3}")
  val wrongResult2: JsValue = Json.parse("{\"id\":18,\"type\":\"result\",\"success\":1234,\"result\":3}")
  test("Parse result 1") {
    ResultUnmarshaller(result1) match {
      case Some(res) =>
        assert(res.success)
        assert(res.result.map(_.as[Int]).contains(3))
      case _ => fail()
    }
  }
  test("Parse wrong results") {
    assert(ResultUnmarshaller(wrongResult1).isEmpty)
    assert(ResultUnmarshaller(wrongResult2).isEmpty)
  }
}
