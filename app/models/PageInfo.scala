package models

case class PageInfo(title: String, url: String)

object PageInfo {
  val default = PageInfo("", "/")
}