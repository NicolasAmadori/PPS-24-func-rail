inThisBuild(
  List(
    version := "2.0.1",
    scalaVersion := "3.6.4",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalacOptions ++= Seq("-Wunused:all", "-Wunused:imports")
  )
)

enablePlugins(ScalafmtPlugin, ScoverageSbtPlugin, AssemblyPlugin)

lazy val root = (project in file("."))
  .settings(
    name := "func-rail",
    mainClass := Some("Main"),
    scalafmtOnCompile := true,
    Test / coverageEnabled := true,
    Compile / coverageEnabled := false,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test",
    libraryDependencies += "org.scalafx" %% "scalafx" % "21.0.0-R32",
    libraryDependencies += "com.brunomnsilva" % "smartgraph" % "2.3.0",
    libraryDependencies += "it.unibo.alice.tuprolog" % "tuprolog" % "3.3.0",
    libraryDependencies ++= {
      val javafxVersion = "21"
      val classifiers   = Seq("linux", "mac", "win")
      val modules       = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
      // for every module and every classifier, emit one dependency
      for {
        m  <- modules
        os <- classifiers
      } yield "org.openjfx" % s"javafx-$m" % javafxVersion classifier os
    },
    Compile / compile := ((Compile / compile) dependsOn scalafmtCheckAll).value,
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )