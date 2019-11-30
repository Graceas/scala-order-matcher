name := "scala-order-matcher"

version := "0.1"

scalaVersion := "2.12.9"
val scalaTestVersion = "3.1.0"

resolvers += "Jitpack" at "https://jitpack.io"
resolvers += "Maven Repository" at "https://repo.artima.com/releases"

libraryDependencies += "org.scalactic" %% "scalactic" % scalaTestVersion % Test
libraryDependencies += "org.scalatest" %% "scalatest" % scalaTestVersion % Test