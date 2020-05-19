package hass.model.state.ground

import org.joda.time.DateTime

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

  def toJoda: org.joda.time.LocalTime = new org.joda.time.LocalTime(h, m, s)
}

object Date {
  def apply(dt: DateTime): Date = Date(dt.getYear, dt.getMonthOfYear, dt.getDayOfMonth)
}

case class Date(y: Int, m: Int, d: Int) extends TimeOrDate {
  override def formatted: String = s"$y-$m-$d"

  override def kind: String = "date"

  def toJoda: DateTime = new DateTime(y, m, d, 0, 0, 0)
}

object DateAndTime {
  def apply(dt: DateTime): DateAndTime = DateAndTime(Date(dt), Time(dt.toLocalTime))
}

case class DateAndTime(date: Date, time: Time) extends TimeOrDate {
  override def formatted: String = date.formatted + " " + time.formatted

  override def kind: String = "datetime"

  def toJoda: DateTime = new DateTime(date.y, date.m, date.d, time.h, time.m, time.s)
}