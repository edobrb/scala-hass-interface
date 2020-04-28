package hass.parser


import com.github.nscala_time.time.Imports.DateTime
import hass.parser.ImplicitReads._
import play.api.libs.json._

object CommonParser {
  implicit def fromJsLookupResultToOption(res: JsLookupResult): Option[JsValue] = res match {
    case JsDefined(value) => Some(value)
    case _ => None
  }

  def json(name: String)(implicit data: JsValue): Option[JsValue] = data \ name

  def value[T: Reads](name: String)(data: JsValue): Option[T] = json(name)(data) map (_.as[T])

  def str(name: String)(implicit data: JsValue): Option[String] = json(name) map (_.as[String])

  def jsonObj(name: String)(implicit data: JsValue): Option[JsObject] = json(name) match {
    case Some(value: JsObject) => Some(value)
    case _ => None
  }

  def datetime(name: String)(implicit data: JsValue): Option[DateTime] = json(name) map (_.as[DateTime])

  def first[I, O](parsers: Seq[I => Option[O]])(implicit input: I): Option[O] =
    (for (parser <- parsers;
          result <- parser(input)) yield result).headOption

}
