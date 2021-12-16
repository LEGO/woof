package org.legogroup.woof

import java.io.File

case class LogInfo(enclosingClass: String, file: File, lineNumber: Int):
  def prefix: String               = enclosingClass
  def postfix: String              = s"(${getFileName(file)}:${lineNumber + 1})"
  private def getFileName(f: File) = file.getPath.split("/").takeRight(1).mkString
