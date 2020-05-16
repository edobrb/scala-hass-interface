package hass.model.service

import hass.model.Types.ServiceType
import hass.model.state.ground.TurnAction

trait TurnService extends EntitiesService {
  override val service: ServiceType = turn.service

  def turn: TurnAction
}
