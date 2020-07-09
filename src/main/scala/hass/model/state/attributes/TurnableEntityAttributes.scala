package hass.model.state.attributes

import hass.model.entity.StatefulEntity
import hass.model.state.EntityState
import hass.model.state.ground.{Off, On, TurnState, Unavailable}

trait TurnableEntityAttributes extends StatefulEntityAttributes[TurnState] {

  def toBoolean: Option[Boolean] = state.map(_.value) match {
    case Some(On) => Some(true)
    case Some(Off) => Some(false)
    case Some(Unavailable) | None => None
  }
}
