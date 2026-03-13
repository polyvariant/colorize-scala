ThisBuild / tlBaseVersion := "0.4"
ThisBuild / organization := "org.polyvariant"
ThisBuild / organizationName := "Polyvariant"
ThisBuild / startYear := Some(2022)
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(tlGitHubDev("kubukoz", "Jakub Kozłowski"))

def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x.cross(CrossVersion.full))

val compilerPlugins = List(
  crossPlugin("org.polyvariant" % "better-tostring" % "0.3.17")
)

val Scala213 = "2.13.18"

ThisBuild / scalaVersion := Scala213
ThisBuild / crossScalaVersions := Seq("2.12.20", Scala213, "3.3.5")

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / tlFatalWarnings := false

ThisBuild / mergifyStewardConfig ~= (_.map(_.withMergeMinors(true)))

val commonSettings = Seq(
  libraryDependencies ++= compilerPlugins
)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .settings(
    name := "colorize",
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalameta" %%% "munit" % "1.1.0" % Test
    ),
  )

lazy val site = project
  .in(file("site"))
  .settings(
    mdocOut := file("."),
    mdocVariables := Map(
      "VERSION" -> {
        if (isSnapshot.value)
          tlLatestVersion.value.getOrElse("0.1.0" /* first planned release */ )
        else
          version.value
      }
    ),
    ThisBuild / githubWorkflowBuild +=
      WorkflowStep.Sbt(
        List("site/mdoc --check"),
        cond = Some(s"matrix.scala == '$Scala213'"),
      ),
  )
  .dependsOn(core.jvm)
  .enablePlugins(MdocPlugin, NoPublishPlugin)

lazy val root = tlCrossRootProject
  .aggregate(core)
