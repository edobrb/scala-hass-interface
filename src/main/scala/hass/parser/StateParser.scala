package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity._
import hass.model.state._
import hass.parser.CommonParser._
import hass.parser.ImplicitReads._
import play.api.libs.json._

object StateParser {
  def parsers: Seq[JsValue => Option[EntityState[_]]] =
    Seq(sensorStateParser,
      switchStateParser,
      lightStateParser,
      inputBooleanStateParser,
      inputDateTimeStateParser,
      unknownEntityParser)

  def parse(data: JsValue): Option[EntityState[_]] =
    for (state <- first(parsers)(data)) yield state

  def switchStateParser(data: JsValue): Option[SwitchState] =
    expectedStateParser(Switch.domain, SwitchState.apply, data)

  def lightStateParser(data: JsValue): Option[LightState] =
    expectedStateParser(Light.domain, LightState.apply, data)

  def sensorStateParser(data: JsValue): Option[SensorState] =
    expectedStateParser(Sensor.domain, SensorState.apply, data)

  def inputBooleanStateParser(data: JsValue): Option[InputBooleanState] =
    expectedStateParser(InputBoolean.domain, InputBooleanState.apply, data)

  def inputDateTimeStateParser(data: JsValue): Option[InputDateTimeState] =
    expectedStateParser(InputDateTime.domain, InputDateTimeState.apply, data)

  def expectedStateParser[T, K](expectedDomain: String, f: (String, T, DateTime, DateTime, Option[JsObject]) => K, data: JsValue)(implicit reads: Reads[T]): Option[K] =
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- stateParser[T](data);
         if domain == expectedDomain)
      yield f(entityName, state, lastChanged, lastUpdated, attributes)

  def stateParser[T](data: JsValue)(implicit reads: Reads[T]): Option[(String, String, T, DateTime, DateTime, Option[JsObject])] = {
    for (entityId <- str("entity_id")(data);
         (domain, name) <- entityIds(entityId);
         lastChanged <- datetime("last_changed")(data);
         lastUpdated <- datetime("last_updated")(data);
         state <- value[T]("state")(data);
         attributes = jsonObj("attributes")(data))
      yield (domain, name, state, lastChanged, lastUpdated, attributes)
  }

  private def unknownEntityParser(data: JsValue): Option[UnknownEntityState] =
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- stateParser[String](data))
      yield UnknownEntityState(domain + "." + entityName, state, lastChanged, lastUpdated, attributes)
}
