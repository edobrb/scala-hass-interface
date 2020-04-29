package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.state.InputDateTimeState
import org.joda.time.{DateTime, LocalTime}

object InputDateTime extends MetaDomain {
  def domain: DomainType = "input_datetime"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): InputDateTime = InputDateTime(light_name.value)(hass)
}

case class InputDateTime(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[Either[DateTime, LocalTime], InputDateTimeState]() with InputDateTime.Domain
