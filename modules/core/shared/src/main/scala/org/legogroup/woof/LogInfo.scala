package org.legogroup.woof

case class LogInfo(enclosingClass: EnclosingClass, fileName: String, lineNumber: Int):
  def prefix: String  = enclosingClass.printableName
  def postfix: String = s"(${fileName}:${lineNumber + 1})"
