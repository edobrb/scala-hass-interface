package hass.unmarshaller


import com.github.nscala_time.time.Imports.DateTime
import hass.model.Types.ServiceType
import hass.model.state.ground.{Off, On, Toggle, TurnAction}
import hass.unmarshaller.ImplicitReads._
import play.api.libs.json._

object CommonUnmarshaller {

  type Unmarshaller[-I, +O] = I => Option[O]
  type JsonUnmarshaller[+O] = Unmarshaller[JsValue, O]

  implicit class RichUnmarshaller[I, O](p: Unmarshaller[I, O]) {
    def map[T](f: O => T): Unmarshaller[I, T] = i => p(i).map(f)
  }

  implicit def fromJsLookupResultToOption(res: JsLookupResult): Option[JsValue] = Some(res) collect {
    case JsDefined(value) => value
  }

  def extract[T: Reads]: JsonUnmarshaller[T] = _.asOpt[T]

  def str(name: String): JsonUnmarshaller[String] = value[String](name)

  def strSeq(name: String): JsonUnmarshaller[Seq[String]] = value[Seq[String]](name)

  def strOrStrSeq(name: String): JsonUnmarshaller[Seq[String]] = first(Seq(str(name).map(s => Seq(s)), strSeq(name)))

  def number(name: String): JsonUnmarshaller[BigDecimal] = value[BigDecimal](name)

  def value[T: Reads](name: String): JsonUnmarshaller[T] = data => json(name)(data) flatMap (_.asOpt[T])

  def json(name: String): JsonUnmarshaller[JsValue] = _ \ name

  def long(name: String): JsonUnmarshaller[Long] = value[Long](name)

  def int(name: String): JsonUnmarshaller[Int] = value[Int](name)

  def bool(name: String): JsonUnmarshaller[Boolean] = value[Boolean](name)

  def datetime(name: String): JsonUnmarshaller[DateTime] = value[DateTime](name)

  def turnAction: Unmarshaller[ServiceType, TurnAction] = st => Some(st) collect {
    case On.service => On
    case Off.service => Off
    case Toggle.service => Toggle
  }

  def jsonObj(name: String): JsonUnmarshaller[JsObject] = data => json(name)(data) collect {
    case value: JsObject => value
  }

  def entityIdsSeq: Unmarshaller[Seq[String], Seq[(String, String)]] = ids =>
    Some(for (id <- ids; (domain, name) <- entityIds(id)) yield (domain, name))

  def entityIds: Unmarshaller[String, (String, String)] = entityId =>
    Some(entityId.split('.').toList) collect {
      case domain :: name :: Nil => (domain, name)
    }

  def first[I, O](parsers: Seq[I => Option[O]]): Unmarshaller[I, O] = input =>
    (for (parser <- parsers; result <- parser(input)) yield result).headOption
}
