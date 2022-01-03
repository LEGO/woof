package org.legogroup.woof

import scala.annotation.tailrec

case class EnclosingClass(fullName: String, lineLength: Int = 80):
  def printableName: String = reduceNameLength(fullName)

  @tailrec
  private def reduceNameLength(name: String, chomps: Int = 0): String = {
    val names = name.split('.')
    val isTooLong = name.length > lineLength
    val notLastPossibleChomp = chomps < names.length - 1
    val keepChomping = notLastPossibleChomp && isTooLong
    if (keepChomping) {
      names.update(chomps, names(chomps).head.toString)
      reduceNameLength(names.mkString("."), chomps + 1)
    } else {
      name
    }
  }
