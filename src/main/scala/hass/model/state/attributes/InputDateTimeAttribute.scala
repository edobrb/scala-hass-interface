package hass.model.state.attributes

import hass.model.state.ground.TimeOrDate

trait InputDateTimeAttribute extends StatefulEntityAttributes[TimeOrDate] {

  def hasTime: Option[Boolean] = attribute[Boolean]("has_time")

  def hasDate: Option[Boolean] = attribute[Boolean]("has_date")
}
