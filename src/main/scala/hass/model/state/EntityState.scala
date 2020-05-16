package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import play.api.libs.json.{JsObject, Reads}

trait EntityState[T] {
  def entity_name: String

  def domain: String

  def entity_id: String = s"$domain.$entity_name"

  def lastChanged: DateTime

  def lastUpdated: DateTime

  def state: T

  def attributes: Option[JsObject]

  def attribute[A: Reads](name: String): Option[A] =
    attributes.flatMap(_.fields.collectFirst { case (`name`, value) => value }).map(_.as[A])
}

object EntityState {
  def unapply(arg: EntityState[_]): Option[(String, Any, DateTime, DateTime, Option[JsObject])] =
    Some(arg.entity_id, arg.state, arg.lastChanged, arg.lastUpdated, arg.attributes)
}












