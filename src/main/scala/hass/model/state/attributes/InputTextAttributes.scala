package hass.model.state.attributes


trait InputTextAttributes extends StatefulEntityAttributes[String] {

  def editable: Option[Boolean] = attribute[Boolean]("editable")

  def min: Option[Int] = attribute[Int]("min")

  def max: Option[Int] = attribute[Int]("max")

  def pattern: Option[String] = attribute[String]("pattern")

  def mode: Option[String] = attribute[String]("mode")
}