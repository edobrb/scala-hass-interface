package hass.parser


import com.github.nscala_time.time.Imports.DateTime
import hass.parser.ImplicitReads._
import play.api.libs.json._

object CommonParser {
  type Parser[-I, +O] = I => Option[O]
  type JsonParser[+O] = Parser[JsValue, O]

  implicit def fromJsLookupResultToOption(res: JsLookupResult): Option[JsValue] = res match {
    case JsDefined(value) => Some(value)
    case _ => None
  }

  def json(name: String): JsonParser[JsValue] = _ \ name

  def value[T: Reads](name: String): JsonParser[T] = data => json(name)(data) flatMap (_.asOpt[T])

  def str(name: String): JsonParser[String] = data => json(name)(data) flatMap (_.asOpt[String])

  def number(name: String): JsonParser[BigDecimal] = data => json(name)(data) flatMap (_.asOpt[BigDecimal])

  def long(name: String): JsonParser[Long] = data => json(name)(data) flatMap (_.asOpt[Long])

  def bool(name: String): JsonParser[Boolean] = data => json(name)(data) flatMap (_.asOpt[Boolean])

  def datetime(name: String): JsonParser[DateTime] = data => json(name)(data) map (_.as[DateTime])

  def jsonObj(name: String): JsonParser[JsObject] = data => json(name)(data) match {
    case Some(value: JsObject) => Some(value)
    case _ => None
  }

  def entityIds: Parser[String, (String, String)] = entityId => {
    val spl = entityId.split('.')
    if (spl.length == 2) {
      Some((spl(0), spl(1)))
    } else {
      None
    }
  }

  def first[I, O](parsers: Seq[I => Option[O]]): Parser[I, O] = input =>
    (for (parser <- parsers;
          result <- parser(input)) yield result).headOption
}
