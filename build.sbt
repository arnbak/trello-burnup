import play.PlayImport.PlayKeys
import JsEngineKeys._

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

name := """trello-burnup"""

version := "1.0-SNAPSHOT"

WebKeys.webTarget := target.value / "scala-web"

artifactPath in PlayKeys.playPackageAssets := WebKeys.webTarget.value / (artifactPath in PlayKeys.playPackageAssets).value.getName

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1",

  "org.webjars" %% "webjars-play" % "2.3.0-2",
  "org.webjars" % "bootstrap" % "3.3.4",
  "org.webjars" % "bootstrap-sass" % "3.3.1",
  "org.webjars" % "font-awesome" % "4.3.0-1",
  "org.webjars" % "angularjs" % "1.3.15",
  "org.webjars" % "angular-ui-router" % "0.2.13",
  "org.webjars" % "highcharts-ng" % "0.0.8",
  "org.webjars" % "highcharts" % "4.1.4",

  "org.apache.commons" % "commons-math3" % "3.3"
)

pipelineStages := Seq(digest, gzip)