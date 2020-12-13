package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.state.PersonState
import hass.model.state.attributes.PersonAttributes


object Person extends MetaDomain {
  override val domain: DomainType = "person"

  def apply()(implicit name: sourcecode.Name, hass: Hass): Person = Person(name.value)(hass)
}

case class Person(entityName: String)(implicit val hass: Hass)
  extends StatefulEntity[String, PersonState]() with Person.Domain
    with PersonAttributes
