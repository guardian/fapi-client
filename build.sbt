
lazy val root = (project in file(".")).
  settings(
    name := "fapi-client",
    version := "0.1.0",
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      "com.gu" %% "content-api-client" % "3.8-SNAPSHOT",
      "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
      "com.gu" %% "facia-api-client" % "0.14"
    ),
    resolvers ++= Seq(
      Resolver.file("Local", file( Path.userHome.absolutePath + "/.ivy2/local"))(Resolver.ivyStylePatterns),
      "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
      )
  )
