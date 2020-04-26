package hass.model.entity


import hass.controller.Hass
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.result.Result
import hass.model.service.{LightToggleService, LightTurnService}
import hass.model.state.{LightState, TurnState}

import scala.concurrent.Future

object Light {
  def apply()(implicit light_name: sourcecode.Name, hass: Hass): Light = Light(light_name.value)(hass)
}

case class Light(entity_name: String)(implicit hass: Hass) extends TurnableEntity with Observable[TurnState] {
  private var _state: LightState = hass.stateOf(entity_id).getOrElse(LightState.unknown(entity_name))

  override def entity_domain: String = "light"

  hass.onEvent {
    case StateChangedEvent(id, _, newState: LightState, _, _) if id == entity_id =>
      _state = newState
      notifyObservers(newState.state)
  }

  def state: LightState = _state

  override def turn(state: TurnState): Future[Result] = hass call LightTurnService(entity_name, state)

  override def toggle: Future[Result] = hass call LightToggleService(entity_name)

  def onChange(f: PartialFunction[TurnState, Unit]): Unit = addObserver(f)
}
