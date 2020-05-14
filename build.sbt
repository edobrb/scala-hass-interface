name := "ScalaHassInterface"

version := "0.1"

scalaVersion := "2.12.11"

resolvers += Resolver.mavenLocal

libraryDependencies += "com.github.andyglow" %% "websocket-scala-client" % "0.3.0" % Compile
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.1"
libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.2.1"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.24.0"

libraryDependencies += "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1"


