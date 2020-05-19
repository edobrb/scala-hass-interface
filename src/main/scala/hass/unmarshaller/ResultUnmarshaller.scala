package hass.unmarshaller

import hass.model.service.Result
import hass.unmarshaller.CommonUnmarshaller._
import play.api.libs.json.JsValue


object ResultUnmarshaller extends JsonUnmarshaller[Result] {

  override def apply(data: JsValue): Option[Result] =
    for ("result" <- str("type")(data);
         success <- bool("success")(data);
         result = json("result")(data))
      yield Result(success, result)
}
