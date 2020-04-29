package hass.model.state

import hass.model.MetaService
import hass.model.Types.ServiceType

sealed trait TurnState {
  def unary_! : TurnState
}

case object On extends TurnState with MetaService{
  def unary_! : TurnState = Off
  override def service: ServiceType = "turn_on"
}

case object Off extends TurnState with MetaService{
  def unary_! : TurnState = On
  override def service: ServiceType = "turn_ff"
}

case object Unavailable extends TurnState with MetaService{
  def unary_! : TurnState = Unavailable
  override def service: ServiceType = "toggle"
}

case object Toggle extends MetaService{
  override def service: ServiceType = "toggle"
}