package hass.parser


import com.github.nscala_time.time.Imports.DateTime
import hass.parser.ImplicitReads._
import play.api.libs.json._

object CommonParser {
  type Parser[T] = JsValue => Option[T]

  implicit def fromJsLookupResultToOption(res: JsLookupResult): Option[JsValue] = res match {
    case JsDefined(value) => Some(value)
    case _ => None
  }

  def json(name: String)(implicit data: JsValue): Option[JsValue] = data \ name

  def json2(name: String): Parser[JsValue] = _ \ name

  def value[T: Reads](name: String)(data: JsValue): Option[T] = json(name)(data) flatMap (_.asOpt[T])

  def str(name: String)(implicit data: JsValue): Option[String] = json(name) flatMap (_.asOpt[String])

  def number(name: String)(implicit data: JsValue): Option[BigDecimal] = json(name) flatMap (_.asOpt[BigDecimal])

  def long(name: String)(implicit data: JsValue): Option[Long] = json(name) flatMap (_.asOpt[Long])

  def bool(name: String)(implicit data: JsValue): Option[Boolean] = json(name) flatMap (_.asOpt[Boolean])

  def jsonObj(name: String)(implicit data: JsValue): Option[JsObject] = json(name) match {
    case Some(value: JsObject) => Some(value)
    case _ => None
  }

  def entityIds(entityId: String): Option[(String, String)] = {
    val spl = entityId.split('.')
    if (spl.length == 2) {
      Some((spl(0), spl(1)))
    } else {
      None
    }
  }

  def datetime(name: String)(implicit data: JsValue): Option[DateTime] = json(name) map (_.as[DateTime])

  def first[I, O](parsers: Seq[I => Option[O]])(implicit input: I): Option[O] =
    (for (parser <- parsers;
          result <- parser(input)) yield result).headOption

}
