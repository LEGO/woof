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
  organization     := "clog",
  githubOwner      := "LEGO",
  githubRepository := "clog",
  githubTokenSource := TokenSource.GitConfig("github.token") || TokenSource.Environment(
    "GITHUB_USERTOKEN",
  ) || TokenSource.Environment("GITHUB_TOKEN"),
)

def clogProject(file: File): Project =
  Project(s"clog-${file.getName()}", file)
    .settings(
      commonSettings,
      name := s"clog-${file.getName()}",
    )

lazy val root =
  project
    .in(file("."))
    .settings(
      commonSettings,
    ) // we have to have a root project, otherwise we cannot override the TokenSource for `sbt-github-packages`

lazy val core =
  clogProject(file("./modules/core"))
    .settings(
      version := "0.1.0-SNAPSHOT",
      scalacOptions ++= Seq("-source", "future"),
      libraryDependencies ++= Seq(
        D.cats,
        D.catsEffect,
        D.munit           % Test,
        D.munitCatsEffect % Test,
      ),
    )

lazy val examples =
  clogProject(file("./modules/examples"))
    .dependsOn(core)

lazy val docs =
  clogProject(file("./modules/usage-docs"))
    .settings(mdocOut := file("."))
    .dependsOn(core)
    .enablePlugins(MdocPlugin)