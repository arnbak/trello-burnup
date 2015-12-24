name := """trello-burnup"""

version := "0.0.2-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  Resolver.sonatypeRepo("snapshots")
)



libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  filters,
  evolutions,
  "com.mohiva" %% "play-silhouette" % "3.0.4",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.play" %% "anorm" % "2.5.0",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.apache.commons" % "commons-math3" % "3.3",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  specs2 % Test,

  "org.webjars" % "font-awesome" % "4.5.0",
  "org.webjars" % "bootstrap" % "3.3.5",
  "org.webjars" % "jquery" % "2.1.4",
  "org.webjars" % "d3js" % "3.5.10",
  "org.webjars" % "nvd3" % "1.8.1"
)

scalariformSettings

routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs)

RjsKeys.mainModule := "main"