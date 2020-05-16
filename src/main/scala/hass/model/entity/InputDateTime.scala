package hass.model.entity

import hass.controller.Hass
import hass.model.Types.{DomainType, ServiceType}
import hass.model.service.{InputDateTimeSetService, Result}
import hass.model.state._
import hass.model.{MetaDomain, MetaService}

import scala.concurrent.Future

object InputDateTime extends MetaDomain with MetaService {
  override def service: ServiceType = "set_datetime"

  override def domain: DomainType = "input_datetime"

  def apply()(implicit light_name: sourcecode.Name, hass: Hass): InputDateTime = InputDateTime(light_name.value)(hass)
}

case class InputDateTime(entity_name: String)(implicit hass: Hass)
  extends StatefulEntity[TimeOrDate, InputDateTimeState]() with InputDateTime.Domain {
  def set(value: TimeOrDate): Future[Result] = hass call InputDateTimeSetService(Seq(entity_name), value)
}
