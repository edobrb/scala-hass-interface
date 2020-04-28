package hass.model.entity

import hass.controller.Hass
import hass.model.state.{InputBooleanState, TurnState}

object InputBoolean extends MetaEntity {
  def domain: String = "input_boolean"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): Light = Light(light_name.value)(hass)
}

case class InputBoolean(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[TurnState, InputBooleanState]() {
  override def meta: MetaEntity = InputBoolean
}
