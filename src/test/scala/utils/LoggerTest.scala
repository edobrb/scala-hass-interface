package utils

import org.scalatest._

class LoggerTest extends FunSuite {
  test("Console logger test") {
    assert(ConsoleLogger.inf("") == ())
    assert(ConsoleLogger.wrn("") == ())
    assert(ConsoleLogger.err("") == ())
  }

  test("Void logger test") {
    assert(VoidLogger.inf("") == ())
    assert(VoidLogger.wrn("") == ())
    assert(VoidLogger.err("") == ())
  }
}
