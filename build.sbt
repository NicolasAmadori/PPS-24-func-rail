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

lazy val root = (project in file("."))
  .settings(
    name := "func-rail",
    scalafmtOnCompile := true,
    coverageEnabled := true,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  )
