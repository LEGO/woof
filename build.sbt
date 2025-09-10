Global / onChangedBuildSource := ReloadOnSourceChanges

val V = new {
  val cats            = "2.13.0"
  val catsEffect      = "3.7.0-RC1"
  val circe           = "0.14.14"
  val http4s          = "0.23.30"
  val munit           = "1.1.1"
  val munitScalacheck = "1.2.0"
  val munitCatsEffect = "2.2.0-RC1"
  val scala           = "3.3.6"
  val slf4j           = "1.7.36"
  val slf4j2          = "2.0.17"
  val tzdb            = "2.6.0"
}

val D = new {
  val slf4jApi  = "org.slf4j" % "slf4j-api" % V.slf4j
  val slf4jApi2 = "org.slf4j" % "slf4j-api" % V.slf4j2

  val catsCore          = Def.setting("org.typelevel" %%% "cats-core" % V.cats)
  val catsEffect        = Def.setting("org.typelevel" %%% "cats-effect" % V.catsEffect)
  val catsEffectTestKit = Def.setting("org.typelevel" %%% "cats-effect-testkit" % V.catsEffect)
  val http4s            = Def.setting("org.http4s" %%% "http4s-core" % V.http4s)
  val munit             = Def.setting("org.scalameta" %%% "munit" % V.munit)
  val munitCatsEffect   = Def.setting("org.typelevel" %%% "munit-cats-effect" % V.munitCatsEffect)
  val munitScalacheck   = Def.setting("org.scalameta" %%% "munit-scalacheck" % V.munitScalacheck)
  val circe             = Def.setting("io.circe" %%% "circe-parser" % V.circe)
  val tzdb              = Def.setting("io.github.cquiroz" %%% "scala-java-time-tzdb" % V.tzdb)
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
  scalacOptions ++= Seq("-source", "future", "-deprecation"),
)

def nameForFile(file: File): String = s"woof-${file.getName()}"

def woofProject(file: File): Project =
  Project(nameForFile(file), file)
    .settings(commonSettings)
    .settings(name := nameForFile(file))

lazy val docs =
  project
    .in(file("docs-target"))
    .settings(commonSettings, mdocIn := file("docs"), mdocOut := file("."), publish / skip := true)
    .enablePlugins(MdocPlugin)
    .dependsOn(core.jvm, http4s.jvm, slf4j)

lazy val root =
  project
    .in(file("."))
    .aggregate(
      List(
        core,
        http4s,
        slf4j,
        slf4j2,
        slf4jCommon,
      ).flatMap(_.componentProjects).map(_.project): _*
    )
    .settings(
      publish / skip := true,
    )

val coreFolder = file("./modules/core")
lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(coreFolder)
  .settings(commonSettings)
  .settings(
    name := nameForFile(coreFolder),
    libraryDependencies ++= Seq(
      D.catsCore.value,
      D.catsEffect.value,
      D.munit.value             % Test,
      D.munitCatsEffect.value   % Test,
      D.catsEffectTestKit.value % Test,
      D.circe.value             % Test,
      D.munitScalacheck.value   % Test,
      D.tzdb.value              % Test
    ),
  )

val http4sFolder = file("./modules/http4s")
lazy val http4s = crossProject(JSPlatform, JVMPlatform /* , NativePlatform */ )
  .crossType(CrossType.Pure)
  .in(http4sFolder)
  .settings(
    name := nameForFile(http4sFolder),
    libraryDependencies += D.http4s.value,
  )
  .settings(commonSettings)
  .dependsOn(core % "compile->compile;test->test") // we also want the test utils

lazy val slf4j = woofProject(file("./modules/slf4j"))
  .settings(libraryDependencies += D.slf4jApi)
  .dependsOn(slf4jCommon % "compile->compile;test->test")

lazy val slf4j2 = woofProject(file("./modules/slf4j-2"))
  .settings(libraryDependencies += D.slf4jApi2)
  .dependsOn(slf4jCommon % "compile->compile;test->test")

lazy val slf4jCommon =
  woofProject(file("./modules/slf4j-common"))
    .dependsOn(core.jvm % "compile->compile;test->test")

lazy val examples = project
  .in(file("./modules/examples"))
  .settings(commonSettings)
  .settings(publish / skip := true)
  .dependsOn(core.jvm)

lazy val examplesJs = crossProject(JSPlatform)
  .withoutSuffixFor(JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("./modules/examples-scalajs"))
  .settings(commonSettings)
  .settings(publish / skip := true)
  .jsConfigure(_.settings(scalaJSUseMainModuleInitializer := true))
  .dependsOn(core)

lazy val benchmarks = project
  .in(file("./modules/benchmarks"))
  .dependsOn(core.jvm)
  .enablePlugins(JmhPlugin)
  .settings(commonSettings)
  .settings(publish / skip := true)
