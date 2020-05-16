package hass.model.state.ground

import hass.model.MetaService
import hass.model.Types.ServiceType

sealed trait TurnState {
  def unary_! : TurnState
}

sealed trait TurnAction extends MetaService

case object On extends TurnState with TurnAction {
  override val service: ServiceType = "turn_on"

  def unary_! : TurnState = Off
}

case object Off extends TurnState with TurnAction {
  override val service: ServiceType = "turn_off"

  def unary_! : TurnState = On
}

case object Unavailable extends TurnState {
  def unary_! : TurnState = Unavailable
}

case object Toggle extends TurnAction {
  override val service: ServiceType = "toggle"
}


