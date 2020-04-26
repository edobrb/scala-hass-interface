package hass.model.state

sealed trait TurnState {
  def unary_! : TurnState
}

case object On extends TurnState {
  def unary_! : TurnState = Off
}

case object Off extends TurnState {
  def unary_! : TurnState = On
}

case object Unavailable extends TurnState {
  def unary_! : TurnState = Unavailable
}
