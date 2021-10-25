val V = new {
  val scala           = "3.0.2"
  val cats            = "2.6.1"
  val catsEffect      = "3.2.9"
  val http4s          = "0.23.6"
  val munit           = "0.7.29"
  val munitCatsEffect = "1.0.6"
}

val D = new {
  val cats            = "org.typelevel" %% "cats-core"           % V.cats
  val catsEffect      = "org.typelevel" %% "cats-effect"         % V.catsEffect
  val http4s          = "org.http4s"    %% "http4s-core"         % V.http4s
  val munit           = "org.scalameta" %% "munit"               % V.munit
  val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect
}

val commonSettings = Seq(
  scalaVersion     := V.scala,
  version          := "0.1.2",
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

lazy val root =
  project
    .in(file("."))
    .settings(
      commonSettings,
      mdocOut := file("."),
    ) // we have to have a root project, otherwise we cannot override the TokenSource for `sbt-github-packages`
    .enablePlugins(MdocPlugin)
    .dependsOn(core)

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
      D.munit           % Test,
      D.munitCatsEffect % Test,
    ),
  )
  .dependsOn(core % "compile->compile;test->test")// we also want the test utils
