package hass.model.entity

import hass.controller.Hass
import hass.model.MetaDomain
import hass.model.Types.DomainType
import hass.model.service.{InputTextSetService, Result}
import hass.model.state.InputTextState

import scala.concurrent.Future

object InputText extends MetaDomain {
  val domain: DomainType = "input_text"

  def apply()(implicit name: sourcecode.Name, hass: Hass): InputText = InputText(name.value)(hass)
}

case class InputText(entityName: String)(implicit val hass: Hass)
  extends StatefulEntity[String, InputTextState]() with InputText.Domain {
  def set(text: String): Future[Result] = {
    val max = state.flatMap(_.max) match {
      case Some(max) => max
      case None => 255
    }
    hass call InputTextSetService(Seq(entityName), if (text.length > max) text.drop(text.length - max) else text)
  }

  def append(text: String): Future[Result] = {
    value match {
      case Some(value) => set(value + text)
      case None => set(text)
    }
  }
}
