package hass.unmarshaller

import com.github.nscala_time.time.Imports.{DateTime, DateTimeZone}
import hass.model.state.ground._
import hass.unmarshaller.CommonUnmarshaller._
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object ImplicitReads {

  implicit val dateTime: Reads[DateTime] = {
    case JsString(value) => Try(JsSuccess(DateTime.parse(value).toDateTime(DateTimeZone.getDefault()))) match {
      case Failure(_) => Try(JsSuccess(DateTime.parse(value, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDateTime(DateTimeZone.getDefault()))).getOrElse(JsError("DateTime parse failure"))
      case Success(value) => value
    }
    case _ => JsError("Invalid DateTime format")
  }

  implicit val localTime: Reads[LocalTime] = {
    case JsString(value) => Try(JsSuccess(LocalTime.parse(value))).getOrElse(JsError("LocalTime parse failure"))
    case _ => JsError("Invalid LocalTime format")
  }

  implicit val timeOrDate: Reads[TimeOrDate] = {
    case attributes: JsObject =>
      //Reads from attributes of a state
      val timeUnmarshaller1: JsonUnmarshaller[Time] = data =>
        for (true <- bool("has_time")(data);
             hour <- int("hour")(data);
             minute <- int("minute")(data);
             second <- int("second")(data))
          yield Time(hour, minute, second)

      val timeUnmarshaller2: JsonUnmarshaller[Time] = data =>
        for (true <- bool("has_time")(data);
             timestamp <- int("timestamp")(data))
          yield Time(timestamp / 3600, (timestamp % 3600) / 60, (timestamp % 3600) % 60)

      val dateUnmarshaller1: JsonUnmarshaller[Date] = data =>
        for (true <- bool("has_date")(data);
             year <- int("year")(data);
             month <- int("month")(data);
             day <- int("day")(data))
          yield Date(year, month, day)

      val datetimeUnmarshaller1: JsonUnmarshaller[DateAndTime] = data =>
        for (time <- first(Seq(timeUnmarshaller1, timeUnmarshaller2))(data);
             date <- dateUnmarshaller1(data))
          yield DateAndTime(date, time)

      //Reads from serviceData of a service
      val timeUnmarshaller3: JsonUnmarshaller[Time] = data => for (time <- value[LocalTime]("time").apply(data)) yield Time(time)
      val dateUnmarshaller2: JsonUnmarshaller[Date] = data => for (date <- value[DateTime]("date").apply(data)) yield Date(date)
      val datetimeUnmarshaller2: JsonUnmarshaller[DateAndTime] = data =>
        for (time <- timeUnmarshaller2(data);
             date <- dateUnmarshaller2(data))
          yield DateAndTime(date, time)
      val datetimeUnmarshaller3: JsonUnmarshaller[DateAndTime] = data => for (datetime <- value[DateTime]("datetime").apply(data))
        yield DateAndTime(datetime)

      first(Seq(datetimeUnmarshaller1, dateUnmarshaller1, timeUnmarshaller1, timeUnmarshaller3,
        datetimeUnmarshaller2, dateUnmarshaller2, timeUnmarshaller2, datetimeUnmarshaller3))(attributes)
        .map(t => JsSuccess(t))
        .getOrElse(JsError("Invalid TimeOrDate format"))

    case _ => JsError("Invalid TimeOrDate format")
  }

  implicit val turnState: Reads[TurnState] = {
    case JsString("on") => JsSuccess(On)
    case JsString("off") => JsSuccess(Off)
    case JsString("unavailable") => JsSuccess(Unavailable)
    case _ => JsError("Invalid TurnState format")
  }
}
