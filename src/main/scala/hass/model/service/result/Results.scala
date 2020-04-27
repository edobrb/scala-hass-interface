package hass.model.service.result

import hass.model.state.EntityState
import play.api.libs.json.JsValue

sealed trait Result {
  def success: Boolean

  def result: Option[JsValue]
}
object Result {
  def unapply(result: Result):Option[Boolean] = Some(result.success)
}

case class ServiceCallResult(id:BigDecimal, success: Boolean, result:  Option[JsValue]) extends Result

case class FailedParseResult(id:BigDecimal) extends Result{
  override def success: Boolean = false

  override def result: Option[JsValue] = None
}
//case class AuthResult(success: Boolean, raw: JsValue) extends Result

//case class FetchStateResult(success: Boolean, states: Seq[EntityState[_]], raw: JsValue) extends Result
