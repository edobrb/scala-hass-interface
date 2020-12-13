package hass.model.state.attributes

import com.github.nscala_time.time.Imports.DateTime

trait ScriptAttributes extends TurnableEntityAttributes {
  def mode: Option[String] = attribute[String]("mode")

  def lastTriggered: Option[DateTime] = tryMapAttribute[DateTime]("last_triggered", DateTime.parse)
}