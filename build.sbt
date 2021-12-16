val V = new {
  val cats            = "2.6.1"
  val catsEffect      = "3.2.9"
  val http4s          = "0.23.6"
  val javassist       = "3.28.0-GA"
  val munit           = "0.7.29"
  val munitCatsEffect = "1.0.6"
  val scala           = "3.0.2"
  val scalaJavaTime   = "2.3.0"
  val slf4j           = "1.7.32"
}

val D = new {
  val cats              = Def.setting("org.typelevel" %%% "cats-core" % V.cats)
  val catsEffect        = Def.setting("org.typelevel" %%% "cats-effect" % V.catsEffect)
  val http4s            = Def.setting("org.http4s" %%% "http4s-core" % V.http4s)
  val javassist         = "org.javassist" % "javassist" % V.javassist
  val munit             = Def.setting("org.scalameta" %%% "munit" % V.munit)
  val munitCatsEffect   = Def.setting("org.typelevel" %%% "munit-cats-effect-3" % V.munitCatsEffect)
  val scalaJavaTime     = Def.setting("io.github.cquiroz" %%% "scala-java-time" % V.scalaJavaTime)
  val scalaJavaTimeTzDb = Def.setting("io.github.cquiroz" %%% "scala-java-time-tzdb" % V.scalaJavaTime)
  val slf4jApi          = "org.slf4j"     % "slf4j-api" % V.slf4j
}

/*
  CI RELEASE SECTION: BEGIN
 */
inThisBuild(
  List(
    sonatypeCredentialHost := "s01.oss.sonatype.org",
    sonatypeRepository     := "https://s01.oss.sonatype.org/service/local",
    organization           := "org.legogroup",
    homepage               := Some(url("https://github.com/LEGO/woof")),
    licenses               := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "hejfelix",
        "Felix Bjært Hargreaves",
        "dkfepaha@lego.com",
        url("https://github.com/hejfelix"),
      ),
    ),
  ),
)

ThisBuild / versionScheme := Some("early-semver")
/*
  CI RELEASE SECTION: END
 */

val commonSettings = Seq(
  scalaVersion := V.scala,
  organization := "org.legogroup",
)

def woofProject(file: File): Project =
  Project(s"woof-${file.getName()}", file)
    .settings(
      commonSettings,
      name := s"woof-${file.getName()}",
      scalacOptions ++= Seq("-source", "future"),
    )

def woofCrossProject(file: File, crossType: CrossType = CrossType.Pure): sbtcrossproject.CrossProject =
  sbtcrossproject
    .CrossProject(s"woof-${file.getName()}", file)(JVMPlatform, JSPlatform)
    .crossType(crossType)
    .settings(
      commonSettings,
      name := s"woof-${file.getName()}",
      scalacOptions ++= Seq("-source", "future"),
    )

lazy val docs =
  project
    .in(file("docs-target"))
    .settings(commonSettings, mdocIn := file("docs"), mdocOut := file("."), publish / skip := true)
    .enablePlugins(MdocPlugin)
    .dependsOn(core.jvm, http4s.jvm, slf4j)

lazy val root =
  project
    .in(file("."))
    .aggregate(core.jvm, core.js, http4s.jvm, http4s.js, slf4j)
    .settings(
      publish / skip := true,
    )

lazy val core =
  woofCrossProject(file("./modules/core"))
    .settings(
      libraryDependencies ++= Seq(
        D.cats.value,
        D.catsEffect.value,
        D.munit.value           % Test,
        D.munitCatsEffect.value % Test,
      ),
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        D.scalaJavaTime.value,
        D.scalaJavaTimeTzDb.value % Test,
      ),
    )

lazy val http4s = woofCrossProject(file("./modules/http4s"))
  .settings(
    libraryDependencies ++= Seq(
      D.http4s.value,
    ),
  )
  .dependsOn(core % "compile->compile;test->test") // we also want the test utils

lazy val slf4j = woofProject(file("./modules/slf4j"))
  .settings(libraryDependencies ++= Seq(D.slf4jApi))
  .dependsOn(core.jvm % "compile->compile;test->test")
