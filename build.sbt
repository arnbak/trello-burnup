name := """trello-burnup"""

version := "0.2-SNAPSHOT"

scalaVersion := "2.11.7"

//resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

//resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

//resolvers += Resolver.sonatypeRepo("snapshots")

lazy val root = (project in file(".")).enablePlugins(PlayScala)



libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "3.0.0",
  jdbc,
  cache,
  ws,
  filters,
  evolutions,

  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.play" %% "anorm" % "2.5.0",

  "org.webjars" %% "webjars-play" % "2.4.0-1",
//  "org.webjars" % "bootstrap" % "3.3.4",
//  "org.webjars" % "bootstrap-sass" % "3.3.1",
//  "org.webjars" % "font-awesome" % "4.3.0-1",
//  "org.webjars" % "angularjs" % "1.3.15",
//  "org.webjars" % "angular-ui-router" % "0.2.13",
//  "org.webjars" % "angular-nvd3" % "0.1.1",
//  "org.webjars" % "d3js" % "3.5.3",
  "org.apache.commons" % "commons-math3" % "3.3",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24"
)

routesGenerator := InjectedRoutesGenerator

//pipelineStages := Seq(digest, gzip)

scalariformSettings