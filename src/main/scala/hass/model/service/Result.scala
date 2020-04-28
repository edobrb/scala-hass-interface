package hass.model.service

import play.api.libs.json.{JsObject, JsString, JsValue}

object Result {
  def parsingError: Result = Result(success = false, Some(JsObject(Seq(("error", JsString("parsing error."))))))
}

case class Result(success: Boolean, result: Option[JsValue])
