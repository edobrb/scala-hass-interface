package hass.model.entity



import hass.controller.Hass
import hass.model.common.Observable
import hass.model.event.StateChangedEvent
import hass.model.service.result.Result
import hass.model.service.{SwitchToggleService, SwitchTurnService}
import hass.model.state._

import scala.concurrent.Future

object Switch {
  def apply()(implicit switch_name: sourcecode.Name, hass: Hass): Switch = Switch(switch_name.value)(hass)
}


case class Switch(entity_name: String)(implicit hass: Hass) extends TurnableEntity with Observable[TurnState] {
  private var _state: SwitchState = hass.stateOf(entity_id).getOrElse(SwitchState.unknown(entity_name))

  override def entity_domain: String = "switch"

  hass.onEvent {
    case StateChangedEvent(id, _, newState: SwitchState, _, _) if id == entity_id =>
      _state = newState
      notifyObservers(newState.state)
  }

  def state: SwitchState = _state

  override def turn(state: TurnState): Future[Result] = hass call SwitchTurnService(entity_name, state)

  override def toggle: Future[Result] = hass call SwitchToggleService(entity_name)

  def onChange(f: PartialFunction[TurnState, Unit]): Unit = addObserver(f)
}


