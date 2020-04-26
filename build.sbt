name := "ScalaHassInterface"

version := "0.1"

scalaVersion := "2.13.1"

resolvers += Resolver.mavenLocal

libraryDependencies += "com.github.andyglow" %% "websocket-scala-client" % "0.3.0" % Compile
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.8.0"
libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.1.9"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.22.0"

libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.13" % "0.9.1"
/*libraryDependencies += "com.typesafe.akka" %% "akka-http"   % "10.1.11"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.26"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"*/