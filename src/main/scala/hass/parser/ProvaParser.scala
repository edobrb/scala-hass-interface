package hass.parser

object ProvaParser extends App {

  implicit def lol[O](p: Parser[Seq[Char], O]): StrParser[O] = StrParser(p)

  implicit def asd(c: Char): StrParser[Char] = str(c)

  implicit def asd2(s: String): StrParser[String] = str(s)

  object Parser {
    def unit[I, O](out: O): Parser[I, O] = ParserImpl(i => Seq((Right(out), i)))
  }

  trait Parser[I, +O] extends (I => Seq[(Either[String, O], I)]) {
    def run: I => Seq[(Either[String, O], I)]

    override def apply(input: I): Seq[(Either[String, O], I)] = run(input)

    def flatMap[T](f: O => Parser[I, T]): Parser[I, T] = ParserImpl(input => this (input).flatMap({
      case (Right(out), rest) => f(out)(rest)
      case (Left(error), rest) => Seq((Left(error), rest))
    }))

    def map[T](f: O => T): Parser[I, T] =
      ParserImpl(input => this (input) map {
        case (Right(out), rest) => (Right(f(out)), rest)
        case (Left(error), rest) => (Left(error), rest)
      })

    def mapEither[T](f: Either[String, O] => Either[String, T]): Parser[I, T] =
      ParserImpl(input => this (input) map {
        case (Right(out), rest) => (f(Right(out)), rest)
        case (Left(error), rest) => (f(Left(error)), rest)
      })

    def filter(f: O => Boolean): Parser[I, O] =
      ParserImpl(input => this (input).filter {
        case (Right(out), rest) => f(out)
        case (Left(error), rest) => true
      })

    def filterOrError(f: O => Boolean)(err: O => String): Parser[I, O] =
      ParserImpl(input => this (input).collect {
        case (Right(out), rest) if f(out) => (Right(out), rest)
        case (Right(out), rest) => (Left(err(out)), rest)
        case (Left(e), rest) => (Left(e), rest)
      })

    def collectOrError[T](f: PartialFunction[O, T])(err: O => String): Parser[I, T] =
      filterOrError(f.isDefinedAt)(err).map(f)


    def takeWhile(f: O => Boolean): Parser[I, Seq[O]] = this.*.filter(s => s.forall(f))

    def collect[T](f: PartialFunction[O, T]): Parser[I, T] = filter(f.isDefinedAt).map(f)

    def |[C >: O, A <: C](p2: => Parser[I, A]): Parser[I, C] = or[C, O, A, I](this, p2)

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

  def or[A, B <: A, C <: A, I](p1: => Parser[I, B], p2: => Parser[I, C]): Parser[I, A] =
    ParserImpl[I, A](i => p1(i) ++ p2(i))

  case class ParserImpl[I, +O](run: I => Seq[(Either[String, O], I)]) extends Parser[I, O]

  case class StrParser[+O](run: Seq[Char] => Seq[(Either[String, O], Seq[Char])]) extends Parser[Seq[Char], O] {
    def manyBy(divisor:String): StrParser[Seq[O]] = this ~ (str(divisor) ~> this).*  map {
      case (out, outs) => out +: outs
    }
    def manyBy2(divisor:String): StrParser[Seq[O]] = manyBy(divisor).*.map(_.flatten)
  }


  def next[E]: Parser[Seq[E], E] = ParserImpl(s => s.headOption.map(h => LazyList((Right(h), s.tail)))
    .getOrElse(LazyList((Left("Expected an element, found: 'EOF'"), EOF))))

  def str(s: String): StrParser[String] =
    StrParser(input => input.take(s.length) match {
      case v if v equals s.toSeq => LazyList((Right(s), input.drop(s.length)))
      case v if v.size < s.length => LazyList((Left("Expected '" + s + "', found: EOF"), EOF))
      case v => LazyList((Left("Expected '" + s + "', found: '" + v + "'"), input.drop(s.length)))
    })

  def str(c: Char): StrParser[Char] = next[Char].mapEither[Char]({
    case Left(_) => Left("Expected '" + c + "', found: 'EOF'")
    case v => v
  }).filterOrError(_ == c)(v => "Expected '" + c + "', found: '" + v + "'")

  def digit: StrParser[Char] = next[Char].mapEither[Char]({
    case Left(_) => Left("Expected [0-9], found: 'EOF'")
    case v => v
  }).filterOrError(_.isDigit)(c => "Expected [0-9], found: '" + c + "'")


  def digits: StrParser[String] = digit.+.map(_.mkString)

  def id: StrParser[String] = next[Char].filter(_.isLetterOrDigit).+.map(_.mkString)

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
    factor ~ ('^' ~ factor).* map calc

  def factor: StrParser[Double] = decimal | ('(' ~> expr <~ ')')


  trait Animal {
    def name: String
    def parser:StrParser[Animal] = "[Cat=" ~> id.map(Cat) <~ "]"
  }

  case class Cat(name: String) extends Animal

  case class Dog(name: String) extends Animal

  def catParser: StrParser[Cat] = "[Cat=" ~> id.map(Cat) <~ "]"

  def dogParser: StrParser[Dog] = "[Dog=" ~> id.map(Dog) <~ "]"

  def animalParser: StrParser[Animal] = catParser | dogParser

  def animalsParser: StrParser[Seq[Animal]] = animalParser.manyBy2(" and ")

  parse(animalsParser)("[Cat=micio] and [Cat=micio]").foreach(println)

  parse(expr <~ ";")("12*23/5+4*4+(1+3)*4;").foreach(println)

  def parse[T](p: StrParser[T])(str: String): Option[Either[Seq[(String, Seq[Char])], Seq[T]]] = {
    p(str).foldLeft[Option[Either[Seq[(String, Seq[Char])], Seq[T]]]](None) {
      case (None, (Right(res), Nil)) => Some(Right(Seq(res)))
      case (None, (Left(err), rest)) => Some(Left(Seq((err, rest))))
      case (Some(Right(v)), (Left(_), _)) => Some(Right(v))
      case (Some(Left(_)), (Right(res), Nil)) => Some(Right(Seq(res)))
      case (Some(Left(errors)), (Left(err), rest)) =>
        val e = errors :+ (err, rest)
        val s = e.map(_._2).minBy(_.size).size
        if (s == 0 && e.exists(_._2 == EOF)) {
          Some(Left(e.filter(_._2.size == s).filter(_._2.toString == "EOF")))
        } else {
          Some(Left(e.filter(_._2.size == s)))
        }
      case (Some(Right(results)), (Right(res), Nil)) => Some(Right(results :+ res))

      case (a, (Right(res), rest)) => a //incomplete solution
    }
  }


  def calc: ((Double, Seq[(Char, Double)])) => Double = {
    case (decimal, seq) => seq.foldLeft(decimal) {
      case (value, ('*', n)) => value * n
      case (value, ('/', n)) => value / n
      case (value, ('+', n)) => value + n
      case (value, ('-', n)) => value - n
      case (value, ('^', n)) => Math.pow(value, n)
    }
  }
}
