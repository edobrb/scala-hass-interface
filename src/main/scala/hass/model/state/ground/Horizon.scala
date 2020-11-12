package hass.model.state.ground

sealed trait Horizon

case object BelowHorizon extends Horizon

case object AboveHorizon extends Horizon
