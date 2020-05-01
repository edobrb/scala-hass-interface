package hass.parser

object ProvaParser extends App {

  implicit def lol[O](p: Parser[Seq[Char], O]): StrParser[O] = StrParser(p)

  implicit def asd(c: Char): StrParser[Char] = str(c)

  implicit def asd2(s: String): StrParser[String] = str(s)

  object Parser {
    def unit[I, O](out: O): Parser[I, O] = ParserImpl(i => Seq((out, i)))
  }

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

    def takeWhile(f: O => Boolean): Parser[I, Seq[O]] = this.*.filter(s => s.forall(f))

    def collect[T](f: PartialFunction[O, T]): Parser[I, T] = filter(f.isDefinedAt).map(f)

    def |[A >: O](p2: => Parser[I, A]): Parser[I, O] = ParserImpl(i => this(i) ++ p2(i).map(_.asInstanceOf[(O,I)]))

    def ||[T](p2: => Parser[I, T]): Parser[I, Either[O, T]] = ParserImpl(i =>
      this.map[Either[O, T]](v => Left(v))(i) ++
        p2.map[Either[O, T]](v => Right(v))(i))

    def ~[T](p2: => Parser[I, T]): Parser[I, (O, T)] = for (a <- this; b <- p2) yield (a, b)

    def ~>[T](p2: => Parser[I, T]): Parser[I, T] = (this ~ p2).map(_._2)

    def <~[T](p2: => Parser[I, T]): Parser[I, O] = (this ~ p2).map(_._1)

    def ? : Parser[I, Option[O]] = (for (v <- this) yield Some(v)) | Parser.unit(None)

    def * : Parser[I, Seq[O]] =
      (for (out <- this; rest <- this.*) yield out +: rest) | Parser.unit(Seq())

    def + : Parser[I, Seq[O]] =
      for (out <- this; rest <- this.*) yield out +: rest
  }

  case class ParserImpl[I, +O](run: I => Seq[(O, I)]) extends Parser[I, O]

  case class StrParser[+O](run: Seq[Char] => Seq[(O, Seq[Char])]) extends Parser[Seq[Char], O]


  def next: StrParser[Char] = StrParser(s => s.headOption.map(h => LazyList((h, s.tail))).getOrElse(Nil))

  def str(c: Char): StrParser[Char] = next.filter(_ == c)

  def str(s: String): StrParser[String] = next.*.collect { case seq if seq.mkString == s => s}
  /*StrParser(input => input.take(s.length) match {
    case v if v equals s.toSeq => LazyList((s, input.drop(s.length)))
    case _ => Nil
  })*/

  def digit: StrParser[Char] = next.filter(_.isDigit)

  def digits: StrParser[String] = digit.+.map(_.mkString)

  def integer: StrParser[Long] = digits.map(_.toLong)

  def decimal: StrParser[Double] =
    integer ~ ('.' ~> digits).? map {
      case (int, Some(dec)) => int + ("0." + dec).toDouble
      case (int, None) => int
    }

  def expr: StrParser[Double] =
    term ~ ('+' ~ term | '-' ~ term).* map calc

  def term: StrParser[Double] =
    power ~ ('*' ~ power | '/' ~ power).* map calc

  def power: StrParser[Double] =
    factor ~ ('^' ~ factor ).* map calc

  def factor: StrParser[Double] = decimal | ('(' ~> expr <~ ')')

  println(digits ("1234a34").toList)

  println(expr("3^3*2+1").collectFirst {
    case (a, Nil) => a
  })

  def calc:((Double, Seq[(Char, Double)])) => Double = {
    case (decimal, seq) => seq.foldLeft(decimal) {
      case (value, ('*', n)) => value * n
      case (value, ('/', n)) => value / n
      case (value, ('+', n)) => value + n
      case (value, ('-', n)) => value - n
      case (value, ('^', n)) => Math.pow(value, n)
    }
  }
}
