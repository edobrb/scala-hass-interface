package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.event.{Event, StateChangedEvent, UnknownEvent}
import hass.parser.ImplicitReads._
import play.api.libs.json._

import scala.util.Try

object EventParser {

  def parse(jsValue: JsValue): Option[Event] =
    Try {
      val origin = (JsPath \ "event" \ "origin").read[String].reads(jsValue).get
      val timeFired = (JsPath \ "event" \ "time_fired").read[DateTime].reads(jsValue).get

      (jsValue \ "event" \ "event_type" match {
        case JsDefined(JsString("state_changed")) =>
          parseStateChangedEvent((jsValue \ "event" \ "data").get, timeFired, origin)
        case _ => None
      }) match {
        case Some(value) =>
          value
        case None => UnknownEvent(jsValue, timeFired, origin)
      }
    }.toOption

  private def parseStateChangedEvent(jsValue: JsValue, timeFired: DateTime, origin: String): Option[StateChangedEvent[_]] =
    Try {
      val entityId = (JsPath \ "entity_id").read[String].reads(jsValue).get
      val oldState = StateParser.parse((jsValue \ "old_state").get).get
      val newState = StateParser.parse((jsValue \ "new_state").get).get
      StateChangedEvent(entityId, oldState, newState, timeFired, origin)
    }.toOption


}