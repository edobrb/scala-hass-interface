package hass.parser

object ProvaParser extends App {

  implicit def lol[O](p: Parser[Seq[Char], O]): StrParser[O] = StrParser(p)

  trait Parser[I, +O] extends (I => Seq[(O, I)]) {
    def run: I => Seq[(O, I)]

    override def apply(input: I): Seq[(O, I)] = run(input)

    def flatMap[T](f: O => Parser[I, T]): Parser[I, T] = ParserImpl(input => this (input) flatMap {
      case (out, rest) => f(out)(rest)
    })

    def map[T](f: O => T): Parser[I, T] = ParserImpl(input => this (input) map {
      case (out, rest) => (f(out), rest)
    })

    def filter(f: O => Boolean): Parser[I, O] =
      ParserImpl(input => this (input).filter { case (o, _) => f(o) })

    def takeWhile(f: O => Boolean):Parser[I, Seq[O]] = many(this).filter(s => s.forall(f))

    def collect[T](f: PartialFunction[O, T]): Parser[I, T] = filter(f.isDefinedAt).map(f)

    def |[A >: O](p2: => Parser[I, A]): Parser[I, O] = ParserImpl(i => this(i) ++ p2(i).map(_.asInstanceOf[(O,I)]))

    def ||[T](p2: => Parser[I, T]): Parser[I, Either[O, T]] = ParserImpl(i =>
      this.map[Either[O, T]](v => Left(v))(i) ++
        p2.map[Either[O, T]](v => Right(v))(i))

    def ~[T](p2: => Parser[I, T]): Parser[I, (O, T)] = for (a <- this; b <- p2) yield (a, b)

    def ~>[T](p2: => Parser[I, T]): Parser[I, T] = (this ~ p2).map(_._2)

    def <~[T](p2: => Parser[I, T]): Parser[I, O] = (this ~ p2).map(_._1)

  }

  case class ParserImpl[I, +O](run: I => Seq[(O, I)]) extends Parser[I, O]

  case class StrParser[+O](run: Seq[Char] => Seq[(O, Seq[Char])]) extends Parser[Seq[Char], O]


  val asd = BigDecimal.apply("23452345.23452345")

  /*def many[I,O](p:Parser[I,O]):Parser[I,Seq[O]] = s => {
    var str = s
    while(p(str).nonEmpty) {

    }
  }*/

  def many[I, O](p: Parser[I, O]): Parser[I, Seq[O]] = {
    type A = Seq[O] //intellij is bugged
    val empty: Parser[I, A] = ParserImpl(i => Seq((Seq(), i)))
    val parser: Parser[I, A] = p.flatMap(out => many(p).map(rest => Seq(out) ++ rest))
    empty | parser
  }

  def maybe[I, O](p: Parser[I, O]): Parser[I, Option[O]] = many(p).map(_.headOption)

  def next: StrParser[Char] =
    StrParser(s => s.headOption.map(h => LazyList((h, s.tail))).getOrElse(Nil))

  def str(c: Char): StrParser[Char] = next.filter(_ == c)

  def str(s: Seq[Char]): StrParser[Seq[Char]] = StrParser(input => input.take(s.length) match {
    case v if v equals s => LazyList((s, input.drop(s.length)))
    case _ => Nil
  })

  def digit: StrParser[Char] = next.filter(_.isDigit)

  def digits: StrParser[String] = many(digit).map(_.mkString).filter(_.nonEmpty)

  def integer: StrParser[BigDecimal] = {
    digits.collect {
      case seq if seq.nonEmpty => BigDecimal(seq.mkString)
    }
  }

  def decimal: StrParser[BigDecimal] =
    (integer ~ maybe(str('.') ~> digits)) map {
      case (int, Some(dec)) => int + BigDecimal("0." + dec)
      case (int, None) => int
    }

  def expr: StrParser[BigDecimal] =
    term ~ many(str('+') ~ term | str('-') ~ term) map {
      case (decimal, seq) => seq.foldLeft(decimal) {
        case (value, ('+', n)) => value + n
        case (value, ('-', n)) => value - n
      }
    }

  def term: StrParser[BigDecimal] =
    factor ~ many(str('*') ~ factor | str('/') ~ factor) map {
      case (decimal, seq) => seq.foldLeft(decimal) {
        case (value, ('*', n)) => value * n
        case (value, ('/', n)) => value / n
      }
    }

  def factor: StrParser[BigDecimal] = decimal | (str('(') ~> expr <~ str(')'))

  println(next.takeWhile(_ != ';').map(_.mkString)("234asd;456").toList)

  println(expr("(10+(20.5*30)/40-7)*10000").collectFirst {
    case (a, Nil) => a
  })
}
