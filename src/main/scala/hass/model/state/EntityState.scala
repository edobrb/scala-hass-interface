package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import play.api.libs.json.JsObject

trait EntityState[T] {
  def entityName: String

  def domain: String

  def entityId: String = s"$domain.$entityName"

  def lastChanged: DateTime

  def lastUpdated: DateTime

  def value: T

  def attributes: Option[JsObject]

  protected def state: Option[EntityState[T]] = Some(this)
}

object EntityState {
  def unapply(arg: EntityState[_]): Option[(String, Any, DateTime, DateTime, Option[JsObject])] =
    Some(arg.entityId, arg.value, arg.lastChanged, arg.lastUpdated, arg.attributes)
}












