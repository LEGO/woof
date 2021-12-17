package org.legogroup.woof

case class LogInfo(enclosingClass: String, fileName: String, lineNumber: Int):
  def prefix: String               = enclosingClass
  def postfix: String              = s"(${fileName}:${lineNumber + 1})"
