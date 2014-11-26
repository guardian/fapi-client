
lazy val root = (project in file(".")).
  settings(
    name := "fapi-client",
    version := "0.1.0",
    scalaVersion := "2.11.4",
    libraryDependencies ++= Seq(
      "com.gu" %% "content-api-client" % "3.7",
      "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
    )
  )
