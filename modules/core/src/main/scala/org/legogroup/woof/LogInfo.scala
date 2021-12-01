package org.legogroup.woof

import java.io.File

case class LogInfo(enclosingClass: String, file: File, lineNumber: Int):
  def prefix: String  = enclosingClass
  def postfix: String = s"(${file.getName}:${lineNumber + 1})"
