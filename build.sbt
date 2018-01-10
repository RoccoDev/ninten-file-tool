name := "ninten-file-tool"

lazy val baseSettings = Seq(
  version := "0.6",
  organization := "com.arbiter34",
  autoScalaLibrary := false,
  crossPaths := false,
  javacOptions in compile ++= Seq("-g:lines,vars,source", "-deprecation"),
  javacOptions in doc += "-Xdoclint:none",
  run := {}
)

lazy val baseDependencies = Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.3"
)

lazy val `file-util` = project
  .settings(name := "file-util")
  .settings(baseSettings)

lazy val `byml-editor-lib` = project
  .dependsOn(`file-util`)
  .settings(name := "byml-editor-lib")
  .settings(libraryDependencies ++= baseDependencies)
  .settings(baseSettings)

lazy val `prod-editor-lib` = project
  .dependsOn(`file-util`)
  .settings(name := "prod-editor-lib")
  .settings(libraryDependencies ++= baseDependencies)
  .settings(baseSettings)

lazy val `yaz0-lib` = project
  .dependsOn(`file-util`)
  .settings(name := "yaz0-lib")
  .settings(baseSettings)

lazy val `ninten-file-tool` = project
  .dependsOn(`byml-editor-lib`, `prod-editor-lib`, `yaz0-lib`)
  .settings(name := "ninten-file-tool")
  .settings(mainClass in assembly := Some("Main"))
  .settings(libraryDependencies += "commons-cli" % "commons-cli" % "1.4")
  .settings(baseSettings)