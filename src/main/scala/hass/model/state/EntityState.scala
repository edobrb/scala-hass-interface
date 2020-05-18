package hass.model.state

import com.github.nscala_time.time.Imports.DateTime
import play.api.libs.json.{JsObject, Reads}

trait EntityState[T] {
  def entityName: String

  def domain: String

  def entityId: String = s"$domain.$entityName"

  def lastChanged: DateTime

  def lastUpdated: DateTime

  def state: T

  def attributes: Option[JsObject]

  def friendlyName: Option[String] = attribute[String]("friendly_name")

  def attribute[A: Reads](name: String): Option[A] =
    attributes.flatMap(_.fields.collectFirst { case (`name`, value) => value }).map(_.as[A])
}

object EntityState {
  def unapply(arg: EntityState[_]): Option[(String, Any, DateTime, DateTime, Option[JsObject])] =
    Some(arg.entityId, arg.state, arg.lastChanged, arg.lastUpdated, arg.attributes)
}












