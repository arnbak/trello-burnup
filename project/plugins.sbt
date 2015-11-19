resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Sonatype snapshots repository
resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.3")

//addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")
//
//addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")
//
//addSbtPlugin("default" % "sbt-sass" % "0.1.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")