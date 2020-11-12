package hass.model.state.attributes

import play.api.libs.json.{JsObject, Reads}

import scala.util.Try


trait EntityAttributes {
  def attributes: Option[JsObject]

  def attribute[T: Reads](name: String): Option[T] =
    for (attrs <- attributes;
         value <- attrs.fields.collectFirst { case (`name`, value) => value };
         result <- Try(value.as[T]).toOption) yield result

  def tryMapAttribute[T](name: String, f: String => T): Option[T] = Try(attribute[String](name).map(f)).toOption.flatten

  def friendlyName: Option[String] = attribute[String]("friendly_name")
}