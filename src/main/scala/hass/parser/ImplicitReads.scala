package hass.parser

import com.github.nscala_time.time.Imports.{DateTime, DateTimeZone}
import hass.model.state.ground._
import hass.parser.CommonParser._
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

  implicit val timeOrDateReads: Reads[TimeOrDate] = {
    case attributes: JsObject =>
      //Reads from attributes of a state
      val timeParser1: JsonParser[Time] = data =>
        for (true <- bool("has_time")(data);
             hour <- int("hour")(data);
             minute <- int("minute")(data);
             second <- int("second")(data))
          yield Time(hour, minute, second)

      val dateParser1: JsonParser[Date] = data =>
        for (true <- bool("has_date")(data);
             year <- int("year")(data);
             month <- int("month")(data);
             day <- int("day")(data))
          yield Date(year, month, day)

      val datetimeParser1: JsonParser[DateAndTime] = data =>
        for (time <- timeParser1(data);
             date <- dateParser1(data))
          yield DateAndTime(date, time)

      //Reads from serviceData of a service
      val timeParser2: JsonParser[Time] = data => for (time <- value[LocalTime]("time").apply(data)) yield Time(time)
      val dateParser2: JsonParser[Date] = data => for (date <- value[DateTime]("date").apply(data)) yield Date(date)
      val datetimeParser2: JsonParser[DateAndTime] = data =>
        for (time <- timeParser2(data);
             date <- dateParser2(data))
          yield DateAndTime(date, time)
      val datetimeParser3: JsonParser[DateAndTime] = data => for (datetime <- value[DateTime]("datetime").apply(data))
        yield DateAndTime(datetime)

      first(Seq(datetimeParser1, dateParser1, timeParser1,
        datetimeParser2, dateParser2, timeParser2, datetimeParser3))(attributes)
        .map(t => JsSuccess(t))
        .getOrElse(JsError("Invalid TimeOrDate format"))

    case _ => JsError("Invalid TimeOrDate format")
  }

  implicit val turnStateReads: Reads[TurnState] = {
    case JsString("on") => JsSuccess(On)
    case JsString("off") => JsSuccess(Off)
    case JsString("unavailable") => JsSuccess(Unavailable)
    case _ => JsError("Invalid TurnState format")
  }
}
