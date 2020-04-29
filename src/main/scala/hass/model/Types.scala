package hass.model

import hass.model.Types._


object Types {
  type Domain = String
  type ServiceType = String
}

trait MetaDomain {
  def domain: Domain

  trait DomainMeta extends MetaDomain {
    override def domain: ServiceType = MetaDomain.this.domain
  }
}

trait MetaService {
  def service: ServiceType

  trait ServiceMeta extends MetaService {
    override def service: ServiceType = MetaService.this.service
  }
}
