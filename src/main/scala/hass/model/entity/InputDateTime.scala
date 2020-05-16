package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.{InputDateTimeSetService, Result}
import hass.model.state._
import org.joda.time.{DateTime, LocalTime}

import scala.concurrent.Future

object InputDateTime extends MetaDomain {
  def domain: DomainType = "input_datetime"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): InputDateTime = InputDateTime(light_name.value)(hass)
}

case class InputDateTime(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[Either[DateTime, LocalTime], InputDateTimeState]() with InputDateTime.Domain {
  private def set(value: TimeOrDate): Future[Result] = hass call InputDateTimeSetService(Seq(entity_name), value)

  def setTime(localTime: LocalTime): Future[Result] = set(Time(localTime))

  def setDatetime(dateTime: DateTime): Future[Result] = set(Datetime(dateTime))

  def setDate(date: DateTime): Future[Result] = set(Date(date))
}
