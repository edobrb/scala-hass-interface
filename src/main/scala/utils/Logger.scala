package utils

import org.joda.time.DateTime

object Logger {
  var log: Logger = ConsoleLogger
}

trait Logger {
  def inf(msg: String): Unit //Info
  def wrn(msg: String): Unit //Warning
  def err(msg: String): Unit //Error
}

object ConsoleLogger extends Logger {
  def inf(msg: String): Unit = {
    println(s"I[${DateTime.now}] $msg")
  }

  def wrn(msg: String): Unit = {
    println(s"W[${DateTime.now}] $msg")
  }

  def err(msg: String): Unit = {
    println(s"E[${DateTime.now}] $msg")
  }
}

