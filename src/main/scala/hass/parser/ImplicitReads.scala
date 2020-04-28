package hass.parser

import com.github.nscala_time.time.Imports.{DateTime, DateTimeZone}
import hass.model.state.{Off, On, TurnState, Unavailable}
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object ImplicitReads {
  implicit val dateTimeReads: Reads[DateTime] = {
    case JsString(value) => Try(JsSuccess(DateTime.parse(value).toDateTime(DateTimeZone.getDefault()))) match {
      case Failure(_) => Try(JsSuccess(DateTime.parse(value, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDateTime(DateTimeZone.getDefault()))).getOrElse(JsError("DateTime parse failure"))
      case Success(value) => value
    }
    case _ => JsError("Invalid DateTime format")
  }
  implicit val localTimeReads: Reads[LocalTime] = {
    case JsString(value) => Try(JsSuccess(LocalTime.parse(value))).getOrElse(JsError("LocalTime parse failure"))
    case _ => JsError("Invalid LocalTime format")
  }
  implicit val eitherDateTimeLocalTimeReads: Reads[Either[DateTime, LocalTime]] = {
    case value: JsValue => localTimeReads.reads(value) match {
      case success: JsSuccess[LocalTime] => success.map(time => Right[DateTime, LocalTime](time))
      case JsError(_) => dateTimeReads.reads(value) match {
        case success: JsSuccess[DateTime] => success.map(date => Left[DateTime, LocalTime](date))
        case JsError(_) => JsError("Invalid LocalTime and DateTime format")
      }
    }
    case _ => JsError("Invalid LocalTime format")
  }
  implicit val turnStateReads: Reads[TurnState] = {
    case JsString("on") => JsSuccess(On)
    case JsString("off") => JsSuccess(Off)
    case JsString("unavailable") => JsSuccess(Unavailable)
    case _ => JsError("Invalid TurnState format")
  }
}
