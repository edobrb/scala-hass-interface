package hass.model.state

import java.time.LocalTime

import hass.model.MetaService
import hass.model.Types.ServiceType
import org.joda.time.DateTime

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


sealed trait TimeOrDate {
  def kind: String

  def formatted: String
}

object Time {
  def apply(lt: org.joda.time.LocalTime): Time = Time(lt.getHourOfDay, lt.getMinuteOfHour, lt.getSecondOfMinute)
}

case class Time(h: Int, m: Int, s: Int) extends TimeOrDate {

  override def formatted: String = s"$h:$m:$s"

  override def kind: String = "time"

  def toLocalTime: org.joda.time.LocalTime = new org.joda.time.LocalTime(h, m, s)
}

object Date {
  def apply(dt: DateTime): Date = Date(dt.getYear, dt.getMonthOfYear, dt.getDayOfMonth)
}

case class Date(y: Int, m: Int, d: Int) extends TimeOrDate {
  override def formatted: String = s"$y-$m-$d"

  override def kind: String = "date"

  def toDateTime: DateTime = new DateTime(y, m, d, 0, 0, 0)
}

object DateAndTime {
  def apply(dt: DateTime): DateAndTime = DateAndTime(Date(dt), Time(dt.toLocalTime))
}

case class DateAndTime(date: Date, time: Time) extends TimeOrDate {
  override def formatted: String = date.formatted + " " + time.formatted

  override def kind: String = "datetime"

  def toDateTime: DateTime = new DateTime(date.y, date.m, date.d, time.h, time.m, time.s)
}