package util

import org.joda.time.DateTime

object Config {
  val siteTitle: String = "Release Burn Up"

  def formatDateTime(date: DateTime): String = {
    date.toString("dd-MM-yyyy HH:mm:ss")
  }
}