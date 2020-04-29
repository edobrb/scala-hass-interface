package hass.model.entity


import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.Domain
import hass.model.service.{Result, SwitchToggleService, SwitchTurnOffService, SwitchTurnOnService}
import hass.model.state._

import scala.concurrent.Future

object Switch extends MetaDomain {
  def domain: Domain = "switch"

  def apply()(implicit switch_name: sourcecode.Name, hass: Hass): Switch = Switch(switch_name.value)(hass)
}

case class Switch(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[TurnState, SwitchState]() with Switch.DomainMeta with TurnableEntity {

  override def toggle: Future[Result] = hass call SwitchToggleService(Seq(entity_name))

  override def turnOn: Future[Result] = hass call SwitchTurnOnService(Seq(entity_name))

  override def turnOff: Future[Result] = hass call SwitchTurnOffService(Seq(entity_name))
}


