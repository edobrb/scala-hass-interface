package hass.parser

import com.github.nscala_time.time.Imports.{DateTime, DateTimeZone}
import hass.model.state.{Off, On, TurnState, Unavailable}
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads}

object ImplicitReads {
  implicit val dateTimeReads: Reads[DateTime] = {
    case JsString(value) => JsSuccess(DateTime.parse(value).toDateTime(DateTimeZone.getDefault()))
    case _ => JsError("Invalid DateTime format")
  }
  implicit val turnStateReads: Reads[TurnState] = {
    case JsString("on") => JsSuccess(On)
    case JsString("off") => JsSuccess(Off)
    case JsString("unavailable") => JsSuccess(Unavailable)
    case _ => JsError("Invalid TurnState format")
  }
}
