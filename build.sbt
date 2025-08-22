inThisBuild(
  List(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "3.6.4",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq("-Wunused:all", "-Wunused:imports")
  )
)

enablePlugins(ScalafmtPlugin, ScoverageSbtPlugin, AssemblyPlugin)

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val root = (project in file("."))
  .settings(
    name := "func-rail",
    mainClass := Some("Main"),
    scalafmtOnCompile := true,
    Test / coverageEnabled := true,
    Compile / coverageEnabled := false,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test",
    libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31",
    libraryDependencies += "com.brunomnsilva" % "smartgraph" % "2.3.0",
    libraryDependencies += "it.unibo.alice.tuprolog" % "tuprolog" % "3.3.0",
    libraryDependencies ++= {
      Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
        .map(m => "org.openjfx" % s"javafx-$m" % "19" classifier osName)
    },
    Compile / compile := ((Compile / compile) dependsOn scalafmtCheckAll).value,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )
