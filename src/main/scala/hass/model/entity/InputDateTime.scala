package hass.model.entity

import hass.controller.Hass
import hass.model.state.{InputBooleanState, InputDateTimeState, TurnState}
import org.joda.time.{DateTime, LocalTime}


object InputDateTime extends MetaEntity {
  def domain: String = "input_datetime"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): InputDateTime = InputDateTime(light_name.value)(hass)
}

case class InputDateTime(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[Either[DateTime, LocalTime], InputDateTimeState]() {
  override def meta: MetaEntity = InputDateTime
}
