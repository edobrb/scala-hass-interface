package hass.model.entity

import hass.controller.Hass
import hass.model.Types.{DomainType, ServiceType}
import hass.model.service.{InputDateTimeSetService, Result}
import hass.model.state._
import hass.model.state.ground.TimeOrDate
import hass.model.{MetaDomain, MetaService}

import scala.concurrent.Future

object InputDateTime extends MetaDomain {
  override val domain: DomainType = "input_datetime"

  def apply()(implicit name: sourcecode.Name, hass: Hass): InputDateTime = InputDateTime(name.value)(hass)
}

case class InputDateTime(entityName: String)(implicit hass: Hass)
  extends StatefulEntity[TimeOrDate, InputDateTimeState]() with InputDateTime.Domain {
  def set(value: TimeOrDate): Future[Result] = hass call InputDateTimeSetService(Seq(entityName), value)
}
