lazy val akkaHttpVersion = "10.5.0"
lazy val akkaVersion    = "2.8.0"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

// Run in a separate JVM, to make sure sbt waits until all threads have
// finished before returning.
// If you want to keep the application running while executing other
// sbt tasks, consider https://github.com/spray/sbt-revolver/
fork := true

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "dev.khusanjon",
      scalaVersion := "2.13.12"
    )),
    name := "chat-api-akka",
    idePackagePrefix := Some("dev.khusanjon"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "io.circe" %% "circe-generic" % "0.14.5",
      "com.github.pureconfig" %% "pureconfig" % "0.17.4",
      "ch.qos.logback" % "logback-classic" % "1.4.7",

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.15" % Test
    )
  )
