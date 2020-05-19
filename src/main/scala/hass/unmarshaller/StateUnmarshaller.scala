package hass.unmarshaller

import com.github.nscala_time.time.Imports.DateTime
import hass.model.entity._
import hass.model.state._
import hass.unmarshaller.CommonUnmarshaller._
import hass.unmarshaller.ImplicitReads._
import play.api.libs.json._

object StateUnmarshaller extends JsonUnmarshaller[EntityState[_]] {

  override def apply(data: JsValue): Option[EntityState[_]] = first(all)(data)

  def all: Seq[JsonUnmarshaller[EntityState[_]]] = Seq[JsonUnmarshaller[EntityState[_]]](
    sensor,
    switch,
    light,
    binarySensor,
    inputBoolean,
    inputDateTime,
    unknown)

  def switch: JsonUnmarshaller[SwitchState] =
    expected(Switch.domain, SwitchState.apply)

  def binarySensor: JsonUnmarshaller[BinarySensorState] =
    expected(BinarySensor.domain, BinarySensorState.apply)

  def light: JsonUnmarshaller[LightState] =
    expected(Light.domain, LightState.apply)

  def sensor: JsonUnmarshaller[SensorState] =
    expected(Sensor.domain, SensorState.apply)

  def inputBoolean: JsonUnmarshaller[InputBooleanState] =
    expected(InputBoolean.domain, InputBooleanState.apply)

  def inputDateTime: JsonUnmarshaller[InputDateTimeState] =
    expectedFromAttributes(InputDateTime.domain, InputDateTimeState.apply)

  def unknown: JsonUnmarshaller[UnknownEntityState] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- genericFromState[String](implicitly[Reads[String]])(data))
      yield UnknownEntityState(domain + "." + entityName, state, lastChanged, lastUpdated, attributes)

  def expected[T: Reads, K](expectedDomain: String, f: (String, T, DateTime, DateTime, Option[JsObject]) => K): JsonUnmarshaller[K] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- genericFromState[T](implicitly[Reads[T]])(data)
         if domain == expectedDomain)
      yield f(entityName, state, lastChanged, lastUpdated, attributes)

  def expectedFromAttributes[T: Reads, K](expectedDomain: String, f: (String, T, DateTime, DateTime, Option[JsObject]) => K): JsonUnmarshaller[K] = data =>
    for ((domain, entityName, state, lastChanged, lastUpdated, attributes) <- genericFromAttributes[T](implicitly[Reads[T]])(data)
         if domain == expectedDomain)
      yield f(entityName, state, lastChanged, lastUpdated, attributes)

  def genericFromState[T: Reads]: JsonUnmarshaller[(String, String, T, DateTime, DateTime, Option[JsObject])] = data =>
    for ((domain, name, lastChanged, lastUpdated, attributes) <- defaultInfo(data);
         state <- value[T]("state").apply(data))
      yield (domain, name, state, lastChanged, lastUpdated, attributes)

  def genericFromAttributes[T: Reads]: JsonUnmarshaller[(String, String, T, DateTime, DateTime, Option[JsObject])] = data =>
    for ((domain, name, lastChanged, lastUpdated, attributes) <- defaultInfo(data);
         attrs <- attributes;
         state <- extract[T].apply(attrs))
      yield (domain, name, state, lastChanged, lastUpdated, attributes)

  def defaultInfo: JsonUnmarshaller[(String, String, DateTime, DateTime, Option[JsObject])] = data =>
    for (entityId <- str("entity_id")(data);
         (domain, name) <- entityIds(entityId);
         lastChanged <- datetime("last_changed")(data);
         lastUpdated <- datetime("last_updated")(data);
         attributes = jsonObj("attributes")(data))
      yield (domain, name, lastChanged, lastUpdated, attributes)
}
