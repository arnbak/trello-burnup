package models

import org.joda.time.DateTime

case class DBBoard(id: String, name: String, selected: Boolean, updated: DateTime)