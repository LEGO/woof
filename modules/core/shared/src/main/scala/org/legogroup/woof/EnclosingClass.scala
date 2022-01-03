package org.legogroup.woof

import scala.annotation.tailrec

case class EnclosingClass(fullName: String):
  private val lineLength = 80
  def printableName: String = reduceNameLength(fullName)

  @tailrec
  private def reduceNameLength(name: String, chomps: Int = 0): String = {
    if(name.length > lineLength) {
      val names = name.split('.')
      names.update(chomps, names(chomps).head.toString)
      reduceNameLength(names.mkString("."), chomps + 1)
    } else {
      name
    }
  }
