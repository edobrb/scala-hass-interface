package hass.parser

import hass.model.service.Result
import hass.parser.CommonParser._
import play.api.libs.json.JsValue


object ResultParser extends JsonParser[Result] {

  override def apply(data: JsValue): Option[Result] =
    for ("result" <- str("type")(data);
         success <- bool("success")(data);
         result = json("result")(data))
      yield Result(success, result)
}
