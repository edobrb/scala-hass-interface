package hass.model.state.attributes

import hass.model.state.ground.Horizon

trait PersonAttributes extends StatefulEntityAttributes[String] {

  def latitude: Option[Double] = attribute[Double]("latitude")

  def longitude: Option[Double] = attribute[Double]("longitude")

  def gpsAccuracy: Option[Double] = attribute[Double]("gps_accuracy")

  def source: Option[String] = attribute[String]("source")

  def id: Option[String] = attribute[String]("id")

  def userId: Option[String] = attribute[String]("user_id")

  def editable: Option[Boolean] = attribute[Boolean]("editable")
}
