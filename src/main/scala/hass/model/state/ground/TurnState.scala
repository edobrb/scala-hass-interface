package hass.model.state.ground

import hass.model.MetaService
import hass.model.Types.ServiceType

sealed trait TurnState {
  def unary_! : TurnState
}

sealed trait TurnAction extends MetaService

case object On extends TurnState with TurnAction {
  def unary_! : TurnState = Off

  override def service: ServiceType = "turn_on"
}

case object Off extends TurnState with TurnAction {
  def unary_! : TurnState = On

  override def service: ServiceType = "turn_off"
}

case object Unavailable extends TurnState {
  def unary_! : TurnState = Unavailable
}

case object Toggle extends TurnAction {
  override def service: ServiceType = "toggle"
}


