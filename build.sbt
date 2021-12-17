val V = new {
  val cats            = "2.7.0"
  val catsEffect      = "3.3.0"
  val http4s          = "0.23.6"
  val munit           = "0.7.29"
  val munitCatsEffect = "1.0.6"
  val scala           = "3.1.0"
  val slf4j           = "1.7.32"
}

val D = new {
  val slf4jApi = "org.slf4j" % "slf4j-api" % V.slf4j

  val catsCore        = Def.setting("org.typelevel" %%% "cats-core" % V.cats)
  val catsEffect      = Def.setting("org.typelevel" %%% "cats-effect" % V.catsEffect)
  val http4s          = Def.setting("org.http4s" %%% "http4s-core" % V.http4s)
  val munit           = Def.setting("org.scalameta" %%% "munit" % V.munit)
  val munitCatsEffect = Def.setting("org.typelevel" %%% "munit-cats-effect-3" % V.munitCatsEffect)
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
  scalacOptions ++= Seq("-source", "future"),
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
    .aggregate(core.jvm, core.js, http4s.jvm, http4s.js, slf4j)
    .settings(
      publish / skip := true,
    )

val coreFolder = file("./modules/core")
lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(coreFolder)
  .settings(commonSettings)
  .settings(
    name := nameForFile(coreFolder),
    libraryDependencies ++= Seq(
      D.catsCore.value,
      D.catsEffect.value,
      D.munit.value           % Test,
      D.munitCatsEffect.value % Test,
    ),
  )

val http4sFolder = file("./modules/http4s")
lazy val http4s = crossProject(JSPlatform, JVMPlatform)
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
  .dependsOn(core.jvm % "compile->compile;test->test")
