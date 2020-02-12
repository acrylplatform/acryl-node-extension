name := "node-extension"
version := "0.0.1"

scalaVersion := "2.12.9"
val nodeVersion = "v1.0.4"

lazy val node = ProjectRef(uri(s"git://github.com/acrylplatform/Acryl.git#$nodeVersion"), "node")

lazy val nodeExtension = (project in file("."))
  .dependsOn(node % "compile;runtime->provided")

libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"
