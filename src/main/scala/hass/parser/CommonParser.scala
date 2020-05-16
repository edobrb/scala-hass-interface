package hass.parser


import com.github.nscala_time.time.Imports.DateTime
import hass.model.Types.ServiceType
import hass.model.state.ground.{Off, On, Toggle, TurnAction}
import hass.parser.ImplicitReads._
import play.api.libs.json._

object CommonParser {

  type Parser[-I, +O] = I => Option[O]
  type JsonParser[+O] = Parser[JsValue, O]

  implicit class RichParser[I, O](p: Parser[I, O]) {
    def map[T](f: O => T): Parser[I, T] = i => p(i).map(f)
  }

  implicit def fromJsLookupResultToOption(res: JsLookupResult): Option[JsValue] = res match {
    case JsDefined(value) => Some(value)
    case _ => None
  }

  def json(name: String): JsonParser[JsValue] = _ \ name

  def value[T: Reads](name: String): JsonParser[T] = data => json(name)(data) flatMap (_.asOpt[T])

  def extract[T: Reads]: JsonParser[T] = _.asOpt[T]

  def str(name: String): JsonParser[String] = value[String](name)

  def strSeq(name: String): JsonParser[Seq[String]] = value[Seq[String]](name)

  def strOrStrSeq(name: String): JsonParser[Seq[String]] = first(Seq(str(name).map(s => Seq(s)), strSeq(name)))

  def number(name: String): JsonParser[BigDecimal] = value[BigDecimal](name)

  def long(name: String): JsonParser[Long] = value[Long](name)

  def int(name: String): JsonParser[Int] = value[Int](name)

  def bool(name: String): JsonParser[Boolean] = value[Boolean](name)

  def datetime(name: String): JsonParser[DateTime] = value[DateTime](name)

  def turnAction: Parser[ServiceType, TurnAction] = {
    case v if v == On.service => Some(On)
    case v if v == Off.service => Some(Off)
    case v if v == Toggle.service => Some(Toggle)
    case _ => None
  }

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

  def entityIdsSeq: Parser[Seq[String], Seq[(String, String)]] = ids =>
    Some(for (id <- ids;
              (domain, name) <- entityIds(id))
      yield (domain, name))


  def first[I, O](parsers: Seq[I => Option[O]]): Parser[I, O] = input =>
    (for (parser <- parsers;
          result <- parser(input)) yield result).headOption
}
