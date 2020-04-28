package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.event.{Event, UnknownEvent}
import hass.model.service.result.{Result, ServiceCallResult}

import play.api.libs.json.{JsDefined, JsPath, JsString, JsValue}

import scala.util.Try


object ResultParser {
  def parse(jsValue: JsValue): Option[Result] =
    Try {
      jsValue \ "type" match {
        case JsDefined(JsString("result")) =>
          val id = (JsPath \ "id").read[BigDecimal].reads(jsValue).get
          val success = (JsPath \ "success").read[Boolean].reads(jsValue).get
          Some(ServiceCallResult(id, success,( jsValue \ "result").toOption))
        case _ => None
      }
    }.toOption.flatten
}
