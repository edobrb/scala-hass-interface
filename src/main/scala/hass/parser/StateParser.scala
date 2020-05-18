package hass.parser

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity._
import hass.model.state._
import hass.parser.CommonParser._
import hass.parser.ImplicitReads._
import play.api.libs.json._

object StateParser extends JsonParser[EntityState[_]] {

  override def apply(data: JsValue): Option[EntityState[_]] = first(all)(data)

  def all: Seq[JsonParser[EntityState[_]]] = Seq[JsonParser[EntityState[_]]](
    sensor,
    switch,
    light,
    binarySensor,
    inputBoolean,
    inputDateTime,
    unknown)

  def switch: JsonParser[SwitchState] =
    expected(Switch.domain, SwitchState.apply)

  def binarySensor: JsonParser[BinarySensorState] =
    expected(BinarySensor.domain, BinarySensorState.apply)

  def light: JsonParser[LightState] =
    expected(Light.domain, LightState.apply)

  def sensor: JsonParser[SensorState] =
    expected(Sensor.domain, SensorState.apply)

  def inputBoolean: JsonParser[InputBooleanState] =
    expected(InputBoolean.domain, InputBooleanState.apply)

  def inputDateTime: JsonParser[InputDateTimeState] =
    expectedFromAttributes(InputDateTime.domain, InputDateTimeState.apply)

  def unknown: JsonParser[UnknownEntityState] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- generic[String](implicitly[Reads[String]])(data))
      yield UnknownEntityState(domain + "." + entityName, state, lastChanged, lastUpdated, attributes)

  def expected[T: Reads, K](expectedDomain: String, f: (String, T, DateTime, DateTime, Option[JsObject]) => K): JsonParser[K] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- generic[T](implicitly[Reads[T]])(data)
         if domain == expectedDomain)
      yield f(entityName, state, lastChanged, lastUpdated, attributes)

  def expectedFromAttributes[T: Reads, K](expectedDomain: String, f: (String, T, DateTime, DateTime, Option[JsObject]) => K): JsonParser[K] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- stateParserFromAttributes[T](implicitly[Reads[T]])(data)
         if domain == expectedDomain)
      yield f(entityName, state, lastChanged, lastUpdated, attributes)

  def generic[T: Reads]: JsonParser[(String, String, T, DateTime, DateTime, Option[JsObject])] = data =>
    for ((domain, name, lastChanged, lastUpdated, attributes) <- defaultInfo(data);
         state <- value[T]("state").apply(data))
      yield (domain, name, state, lastChanged, lastUpdated, attributes)

  def stateParserFromAttributes[T: Reads]: JsonParser[(String, String, T, DateTime, DateTime, Option[JsObject])] = data =>
    for ((domain, name, lastChanged, lastUpdated, attributes) <- defaultInfo(data);
         attrs <- attributes;
         state <- extract[T].apply(attrs))
      yield (domain, name, state, lastChanged, lastUpdated, attributes)

  def defaultInfo: JsonParser[(String, String, DateTime, DateTime, Option[JsObject])] = data =>
    for (entityId <- str("entity_id")(data);
         (domain, name) <- entityIds(entityId);
         lastChanged <- datetime("last_changed")(data);
         lastUpdated <- datetime("last_updated")(data);
         attributes = jsonObj("attributes")(data))
      yield (domain, name, lastChanged, lastUpdated, attributes)
}
