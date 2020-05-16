package hass.model.service

import hass.model.Types.ServiceType
import hass.model.state.ground.TurnAction

trait TurnService extends EntitiesService {
  def turn: TurnAction
  override val service: ServiceType = turn.service
}
