package hass.model.group

import hass.controller.Hass
import hass.model.entity.{Light, Turnable}
import hass.model.service.{LightToggleService, LightTurnOffService, LightTurnOnService}

case class LightsGroup(lights: Seq[Light])(override implicit val hass: Hass)
  extends Turnable[LightTurnOnService, LightTurnOffService, LightToggleService] {
  override def onService: LightTurnOnService = LightTurnOnService(lights.map(_.entity_name))

  override def offService: LightTurnOffService = LightTurnOffService(lights.map(_.entity_name))

  override def toggleService: LightToggleService = LightToggleService(lights.map(_.entity_name))
}