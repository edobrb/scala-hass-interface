package hass.model.service

import hass.model.Types.ServiceType
import hass.model.state.TurnAction

trait TurnService extends EntitiesService {
  def turn: TurnAction
  override def service: ServiceType = turn.service
}
