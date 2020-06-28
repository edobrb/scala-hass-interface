package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.{InputBooleanTurnService, InputTextSetService, Result}
import hass.model.state.{InputBooleanState, InputTextState}
import hass.model.state.ground.{TurnAction, TurnState}

import scala.concurrent.Future

object InputText extends MetaDomain {
  val domain: DomainType = "input_text"

  def apply()(implicit name: sourcecode.Name, hass: Hass): InputText = InputText(name.value)(hass)
}

case class InputText(entityName: String)(implicit val hass: Hass)
  extends StatefulEntity[String, InputTextState]() with InputText.Domain {
  def set(text:String): Future[Result] = hass call InputTextSetService(Seq(entityName), text)
}
