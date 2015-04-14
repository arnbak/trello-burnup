resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

// addSbtPlugin("com.typesafe.sbt" % "sbt-stylus" % "1.0.1")

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

//addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.3")