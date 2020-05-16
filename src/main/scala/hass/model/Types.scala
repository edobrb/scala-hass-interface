package hass.model

import hass.model.Types._


object Types {
  type DomainType = String
  type ServiceType = String
}

trait MetaDomain {
  val domain: DomainType

  trait Domain extends MetaDomain {
    override val domain: DomainType = MetaDomain.this.domain
  }

}

trait MetaService {
  val service: ServiceType

  trait Service extends MetaService {
    override val service: ServiceType = MetaService.this.service
  }

}
