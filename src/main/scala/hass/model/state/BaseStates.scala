package hass.model.state

import hass.model.MetaService
import hass.model.Types.ServiceType
import org.joda.time.{DateTime, LocalTime}

sealed trait TurnState {
  def unary_! : TurnState
}

sealed trait TurnAction extends MetaService

case object On extends TurnState with TurnAction  {
  def unary_! : TurnState = Off
  override def service: ServiceType = "turn_on"
}

case object Off extends TurnState with TurnAction {
  def unary_! : TurnState = On

  override def service: ServiceType = "turn_off"
}

case object Unavailable extends TurnState {
  def unary_! : TurnState = Unavailable
}

case object Toggle extends TurnAction {
  override def service: ServiceType = "toggle"
}

trait SetDateTimeMetaService extends MetaService {
  override def service: ServiceType = "set_datetime"
}


sealed trait TimeOrDate {
  def kind:String
  def formatted: String
}
case class Time(value: LocalTime) extends TimeOrDate {

  override def formatted: String = value.getHourOfDay + ":" + value.getMinuteOfHour + ":" + value.getSecondOfMinute

  override def kind: String = "time"
}
case class Date(value: DateTime) extends TimeOrDate {
  override def formatted: String = value.getYear + "-" + value.getMonthOfYear + "-" + value.getDayOfMonth
  override def kind: String = "date"
}
case class Datetime(value: DateTime) extends TimeOrDate {
  override def formatted: String = value.getYear + "-" + value.getMonthOfYear + "-" + value.getDayOfMonth + " " +
    value.getHourOfDay + ":" + value.getMinuteOfHour + ":" + value.getSecondOfMinute
  override def kind: String = "datetime"
}