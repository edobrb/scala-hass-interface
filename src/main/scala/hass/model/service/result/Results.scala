package hass.model.service.result

import hass.model.state.EntityState
import play.api.libs.json.JsValue

sealed trait Result {
  def success: Boolean

  def raw: JsValue
}

case class ServiceCallResult(success: Boolean, raw: JsValue) extends Result

case class AuthResult(success: Boolean, raw: JsValue) extends Result

case class FetchStateResult(success: Boolean, states: Seq[EntityState[_]], raw: JsValue) extends Result
