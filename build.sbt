val V = new {
  val scala           = "3.0.2"
  val cats            = "2.6.1"
  val catsEffect      = "3.2.9"
  val munit           = "0.7.29"
  val munitCatsEffect = "1.0.6"
}

val D = new {
  val cats            = "org.typelevel" %% "cats-core"           % V.cats
  val catsEffect      = "org.typelevel" %% "cats-effect"         % V.catsEffect
  val munit           = "org.scalameta" %% "munit"               % V.munit
  val munitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % V.munitCatsEffect
}

val commonSettings = Seq(
  scalaVersion     := V.scala,
  version          := "0.1.0",
  organization     := "woof",
  githubOwner      := "LEGO",
  githubRepository := "woof",
)

def clogProject(file: File): Project =
  Project(s"woof-${file.getName()}", file)
    .settings(
      commonSettings,
      name := s"woof-${file.getName()}",
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
  clogProject(file("./modules/core"))
    .settings(
      scalacOptions ++= Seq("-source", "future"),
      libraryDependencies ++= Seq(
        D.cats,
        D.catsEffect,
        D.munit           % Test,
        D.munitCatsEffect % Test,
      ),
    )
