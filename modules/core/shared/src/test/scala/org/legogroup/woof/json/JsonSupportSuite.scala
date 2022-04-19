package org.legogroup.woof.json

import org.scalacheck.{Arbitrary, Gen}
import _root_.org.legogroup.woof.json.JsonSupport
import munit.CatsEffectSuite
import org.legogroup.woof.*
import org.legogroup.woof.json.JsonSupportSuite.epochMillisGen
import org.scalacheck.Prop.*
import org.scalacheck.*

class JsonSupportSuite extends munit.ScalaCheckSuite:

  override def scalaCheckTestParameters = super.scalaCheckTestParameters.withMinSuccessfulTests(2000)

  property("Escape characters") {
    val specialChars = "Text with special character /\"'\b\f\t\r\n."
    val expected     = """Text with special character /\"'\b\f\t\r\n."""
    JsonSupport.escape(specialChars) == expected
  }

  property("render LogLine") {
    val logInfo = LogInfo(EnclosingClass("my.enclosing.Class"), "filename", 1337)
    val context = List("hey" -> "dude", "foo" -> "bar")
    val epochMillis = EpochMillis(startTime.toMillis)
    val logLine = LogLine(
      LogLevel.Debug,
      logInfo,
      "my message",
      context
    )

    val js     = JsonSupport()
    val actual = js.toJsonString(logLine, epochMillis)
    val expected =
      """{"level":"Debug","epochMillis":"549459420000","timeStamp":"1987-05-31 13:37:00","enclosingClass":"my.enclosing.Class","message":"my message","hey":"dude","foo":"bar"}"""

    println(actual)
    actual == expected
  }

  property("render arbitrary log lines"){
    forAll(JsonSupportSuite.logLineGen, JsonSupportSuite.epochMillisGen){ (logLine, epochMillis) =>

      val encoded = JsonSupport().toJsonString(logLine, epochMillis)
      val parsed = io.circe.parser.parse(encoded)

      parsed.isRight
    }
  }

  property("escape arbitrary strings") {
    forAll { (s: String) =>
      val escaped  = "\"" + JsonSupport.escape(s) + "\""
      val parsed   = io.circe.parser.parse(escaped)
      val expected = Right(io.circe.Json.fromString(s.filterNot(_.isControl)))
      parsed == expected
    }
  }

end JsonSupportSuite

object JsonSupportSuite:

  import Gen.*
  import Arbitrary.arbitrary

  val logInfoGen =
    for
      enclosingClassName <- Gen.listOf(Gen.alphaNumStr)
      fileName           <- Gen.alphaNumStr
      lineNumber         <- Gen.posNum[Int]
    yield LogInfo(EnclosingClass(enclosingClassName.mkString(".")), fileName, lineNumber)

  val levelGen = Gen.oneOf(LogLevel.values)

  val epochMillisGen = Gen.posNum[Long].map(EpochMillis)

  val logLineGen =
    for
      info    <- logInfoGen
      context <- arbitrary[List[(String, String)]]
      level   <- levelGen
      message <- arbitrary[String]
    yield LogLine(level, info, message, context)

end JsonSupportSuite
