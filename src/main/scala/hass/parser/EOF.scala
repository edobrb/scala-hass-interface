package hass.parser

import scala.collection.Iterator
import scala.collection.immutable.{Nil}

case object EOF extends Seq[Nothing] {
  def apply(i:Int):Nothing = Nil.head
  override def length: Int = 0

  override def iterator: Iterator[Nothing] = new Iterator[Nothing] {
    override def hasNext: Boolean = false

    override def next(): Nothing = Nil.head
  }

  override def toString():String = "EOF"

}

