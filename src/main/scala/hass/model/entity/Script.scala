package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.{Result, ScriptTurnService}
import hass.model.state._
import hass.model.state.ground.{TurnAction, TurnState}

import scala.concurrent.Future

object Script extends MetaDomain {
  override val domain: DomainType = "script"

  def apply()(implicit name: sourcecode.Name, hass: Hass): Script = Script(name.value)(hass)
}

case class Script(entityName: String)(override implicit val hass: Hass)
  extends StatefulEntity[TurnState, ScriptState]() with Script.Domain
    with Turnable[ScriptTurnService] {
  override def service(turn: TurnAction): ScriptTurnService = ScriptTurnService(Seq(entityName), turn)

  def trigger(): Future[Result] = turnOn()
}


