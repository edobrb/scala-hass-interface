package utils

import org.joda.time.DateTime

trait Logger {
  def inf(msg: String): Unit //Info
  def wrn(msg: String): Unit //Warning
  def err(msg: String): Unit //Error
}

object VoidLogger extends Logger {
  override def inf(msg: String): Unit = {}

  override def wrn(msg: String): Unit = {}

  override def err(msg: String): Unit = {}
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

