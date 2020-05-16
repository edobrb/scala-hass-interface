package hass.model.service

import hass.model.Types.{DomainType, ServiceType}
import play.api.libs.json.JsObject

case class UnknownService(override val domain: DomainType, override val service: ServiceType, override val serviceData: JsObject) extends Service
