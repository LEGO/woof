val V = new {
  val cats            = "2.6.1"
  val catsEffect      = "3.2.9"
  val http4s          = "0.23.6"
  val javassist       = "3.28.0-GA"
  val munit           = "0.7.29"
  val munitCatsEffect = "1.0.6"
  val scala           = "3.0.2"
  val slf4j           = "1.7.32"
}

val D = new {
  val cats            = "org.typelevel" %% "cats-core"           % V.cats
  val catsEffect      = "org.typelevel" %% "cats-effect"         % V.catsEffect
  val http4s          = "org.http4s"    %% "http4s-core"         % V.http4s
  val javassist       = "org.javassist"  % "javassist"           % V.javassist
  val munit           = "org.scalameta" %% "munit"               % V.munit
  val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect
  val slf4jApi        = "org.slf4j"      % "slf4j-api"           % V.slf4j
}

val commonSettings = Seq(
  scalaVersion     := V.scala,
  version          := "0.1.7",
  organization     := "woof",
  githubOwner      := "LEGO",
  githubRepository := "woof",
)

def woofProject(file: File): Project =
  Project(s"woof-${file.getName()}", file)
    .settings(
      commonSettings,
      name := s"woof-${file.getName()}",
      scalacOptions ++= Seq("-source", "future"),
    )

lazy val docs =
  project
    .in(file("docs-target"))
    .settings(commonSettings,mdocIn := file("docs"), mdocOut := file("."))
    .enablePlugins(MdocPlugin)
    .dependsOn(core, http4s, slf4j)

lazy val root =
  project
    .in(file("."))
    .aggregate(core, http4s, slf4j)

lazy val core =
  woofProject(file("./modules/core"))
    .settings(
      libraryDependencies ++= Seq(
        D.cats,
        D.catsEffect,
        D.munit           % Test,
        D.munitCatsEffect % Test,
      ),
    )

lazy val http4s = woofProject(file("./modules/http4s"))
  .settings(
    libraryDependencies ++= Seq(
      D.http4s,
    ),
  )
  .dependsOn(core % "compile->compile;test->test") // we also want the test utils

lazy val slf4j = woofProject(file("./modules/slf4j"))
  .settings(libraryDependencies ++= Seq(D.slf4jApi))
  .dependsOn(core % "compile->compile;test->test")
