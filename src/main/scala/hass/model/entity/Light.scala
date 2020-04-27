package hass.model.entity


import hass.controller.Hass
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.result.Result
import hass.model.service.{LightToggleService, LightTurnOffService, LightTurnOnService}
import hass.model.state.{LightState, TurnState}
import javax.xml.crypto.dsig.Transform

import scala.concurrent.Future

object Light {
  def apply()(implicit light_name: sourcecode.Name, hass: Hass): Light = Light(light_name.value)(hass)
}

case class Light(entity_name: String)(implicit hass: Hass) extends TurnableEntity with Observable[LightState] {
  private var _state: LightState = hass.stateOf(entity_id).getOrElse(LightState.unknown(entity_name))

  override def entity_domain: String = "light"

  hass.onEvent {
    case StateChangedEvent(id, _, newState: LightState, _, _) if id == entity_id =>
      _state = newState
      notifyObservers(newState)
  }

  def state: LightState = _state

  override def toggle: Future[Result] = hass call LightToggleService(entity_name)

  override def onTurnStateChange(f: PartialFunction[TurnState, Unit]): Unit = addObserver({
    case LightState(_, turn, _, _, _) if f.isDefinedAt(turn) => f(turn)
  })

  def onStateChange(f: PartialFunction[LightState, Unit]): Unit = addObserver(f)

  /**
   * Turns on the light by calling LightTurnOnService.
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
