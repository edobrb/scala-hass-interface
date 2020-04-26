package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.state._
import hass.parser.ImplicitReads._
import play.api.libs.json.{JsPath, JsValue, Reads}

import scala.util.Try

object StateParser {
  def parse(jsValue: JsValue): Option[EntityState[_]] = Try {
    parseStateAttributes[String](jsValue).get._1.split('.')(0) match {
      case "switch" => parseSwitchState(jsValue)
      case "light" => parseLightState(jsValue)
      case "sensor" => parseSensorState(jsValue)
      case _ => parseUnknownEntityState(jsValue)
    }
  }.toOption

  private def parseUnknownEntityState(jsValue: JsValue): UnknownEntityState = parseStateAttributes[String](jsValue) match {
    case Some((entityId, state, lastChanged, lastUpdated, attributes)) =>
      UnknownEntityState(entityId, state, lastChanged, lastUpdated, attributes)
  }

  private def parseSwitchState(jsValue: JsValue): SwitchState = parseStateAttributes[TurnState](jsValue) match {
    case Some((entityId, state, lastChanged, lastUpdated, attributes)) =>
      SwitchState(entityId.split('.')(1), state, lastChanged, lastUpdated, attributes)
  }

  private def parseLightState(jsValue: JsValue): LightState = parseStateAttributes[TurnState](jsValue) match {
    case Some((entityId, state, lastChanged, lastUpdated, attributes)) =>
      LightState(entityId.split('.')(1), state, lastChanged, lastUpdated, attributes)
  }

  private def parseSensorState(jsValue: JsValue): SensorState = parseStateAttributes[String](jsValue) match {
    case Some((entityId, state, lastChanged, lastUpdated, attributes)) =>
      SensorState(entityId.split('.')(1), state, lastChanged, lastUpdated, attributes)
  }

  private def parseStateAttributes[T](jsValue: JsValue)(implicit reads: Reads[T]): Option[(String, T, DateTime, DateTime, Option[JsValue])] = Try {
    val entityId = (JsPath \ "entity_id").read[String].reads(jsValue).get
    val lastChanged = (JsPath \ "last_changed").read[DateTime].reads(jsValue).get
    val lastUpdated = (JsPath \ "last_updated").read[DateTime].reads(jsValue).get
    val state = (JsPath \ "state").read[T].reads(jsValue).get
    (entityId, state, lastChanged, lastUpdated, (jsValue \ "attributes").toOption)
  }.toOption
}
