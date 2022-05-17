package org.legogroup.woof

class TimeFormatSuite extends munit.FunSuite:

  test("Should render time as iso") {

    val actual   = org.legogroup.woof.isoTimeFormat(EpochMillis(startTime.toMillis + 500))
    val expected = "1987-05-31T11:37:00Z"

    assertEquals(actual, expected)

  }

end TimeFormatSuite
