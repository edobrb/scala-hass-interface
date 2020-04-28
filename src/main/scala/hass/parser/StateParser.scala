package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity._
import hass.model.state._
import hass.parser.CommonParser._
import hass.parser.ImplicitReads._
import play.api.libs.json._

object StateParser extends JsonParser[EntityState[_]] {

  override def apply(data: JsValue): Option[EntityState[_]] = first(parsers)(data)

  def parsers: Seq[JsonParser[EntityState[_]]] = Seq[JsonParser[EntityState[_]]](
    sensorStateParser,
    switchStateParser,
    lightStateParser,
    inputBooleanStateParser,
    inputDateTimeStateParser,
    unknownEntityParser)

  def switchStateParser: JsonParser[SwitchState] =
    expectedStateParser(Switch.domain, SwitchState.apply)

  def lightStateParser: JsonParser[LightState] =
    expectedStateParser(Light.domain, LightState.apply)

  def sensorStateParser: JsonParser[SensorState] =
    expectedStateParser(Sensor.domain, SensorState.apply)

  def inputBooleanStateParser: JsonParser[InputBooleanState] =
    expectedStateParser(InputBoolean.domain, InputBooleanState.apply)

  def inputDateTimeStateParser: JsonParser[InputDateTimeState] =
    expectedStateParser(InputDateTime.domain, InputDateTimeState.apply)

  def unknownEntityParser: JsonParser[UnknownEntityState] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- stateParser[String](implicitly[Reads[String]])(data))
      yield UnknownEntityState(domain + "." + entityName, state, lastChanged, lastUpdated, attributes)

  def expectedStateParser[T: Reads, K](expectedDomain: String, f: (String, T, DateTime, DateTime, Option[JsObject]) => K): JsonParser[K] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- stateParser[T](implicitly[Reads[T]])(data)
         if domain == expectedDomain)
      yield f(entityName, state, lastChanged, lastUpdated, attributes)

  def stateParser[T: Reads]: JsonParser[(String, String, T, DateTime, DateTime, Option[JsObject])] = data =>
    for (entityId <- str("entity_id")(data);
         (domain, name) <- entityIds(entityId);
         lastChanged <- datetime("last_changed")(data);
         lastUpdated <- datetime("last_updated")(data);
         state <- value[T]("state")(implicitly[Reads[T]])(data);
         attributes = jsonObj("attributes")(data))
      yield (domain, name, state, lastChanged, lastUpdated, attributes)
}
