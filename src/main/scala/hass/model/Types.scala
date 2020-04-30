package hass.model

import hass.model.Types._


object Types {
  type DomainType = String
  type ServiceType = String
}

trait Domain[T] {
  def value: DomainType
}

trait MetaDomain {
  def domain: DomainType

  trait Domain extends MetaDomain {
    override def domain: DomainType = MetaDomain.this.domain
  }

}

trait MetaService {
  def service: ServiceType

  trait Service extends MetaService {
    override def service: ServiceType = MetaService.this.service
  }
}
