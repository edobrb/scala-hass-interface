package hass.model.entity


import hass.controller.Hass
import hass.model.service.result.Result
import hass.model.service.{SwitchToggleService, SwitchTurnService}
import hass.model.state._

import scala.concurrent.Future

object Switch extends MetaEntity {
  override def domain: String = "switch"
  def apply()(implicit switch_name: sourcecode.Name, hass: Hass): Switch = Switch(switch_name.value)(hass)
}


case class Switch(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[TurnState, SwitchState]()
    with TurnableEntity {
  override def meta: MetaEntity = Switch

  override def toggle: Future[Result] = hass call SwitchToggleService(entity_name)

  override def turnOn: Future[Result] = hass call SwitchTurnService(entity_name, On)

  override def turnOff: Future[Result] = hass call SwitchTurnService(entity_name, Off)
}


