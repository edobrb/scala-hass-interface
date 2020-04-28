package hass.model.entity


import hass.controller.Hass
import hass.model.service.{LightToggleService, LightTurnOffService, LightTurnOnService, Result}
import hass.model.state.{LightState, TurnState}

import scala.concurrent.Future

object Light extends MetaEntity {
  def domain: String = "light"
  def apply()(implicit light_name: sourcecode.Name, hass: Hass): Light = Light(light_name.value)(hass)
}

case class Light(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[TurnState, LightState]() with
    TurnableEntity {
  override def meta: MetaEntity = Light

  override def toggle: Future[Result] = hass call LightToggleService(entity_name)

  /**
   * Turns on the light by calling LightTurnOnService.
   *
   * @param t will be applied to the default LightTurnOnService. Ex: turnOn(_.brightness(220).rgb(255,0,0))
   * @return the future result of the service call
   */
  def turnOn(t: LightTurnOnService => LightTurnOnService): Future[Result] =
    hass call t(LightTurnOnService(entity_name))

  /**
   * Turns off the light by calling LightTurnOffService.
   * @param t will be applied to the default LightTurnOffService. Ex: turnOff(_.transition(2))
   * @return the future result of the service call
   */
  def turnOff(t: LightTurnOffService => LightTurnOffService): Future[Result] =
    hass call t(LightTurnOffService(entity_name))

  override def turnOn: Future[Result] = turnOn(identity)

  override def turnOff: Future[Result] = turnOff(identity)
}
