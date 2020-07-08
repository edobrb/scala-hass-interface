package utils
import org.scalatest._

class IdDispatcherTest extends FunSuite {
  test("Id Dispatcher test 1") {
    val ids = IdDispatcher(1)
    assert(ids.peekNext == 1)
    assert(ids.next == 1)
    assert(ids.peekNext == 2)
    assert(ids.next == 2)
    assert(ids.peekNext == 3)
    assert(ids.next == 3)
    assert(ids.next == 4)
  }

  test("Id Dispatcher test 2") {
    val ids = IdDispatcher(Long.MaxValue - 1)
    assert(ids.next == Long.MaxValue - 1)
    assert(ids.next == Long.MaxValue)
    assert(ids.next == Long.MaxValue - 1)
    assert(ids.next == Long.MaxValue)
  }
}
