package org.slf4j.impl

import org.legogroup.woof.slf4j.WoofLogger
import org.slf4j.spi.LoggerFactoryBinder
import org.slf4j.{ILoggerFactory, Logger, LoggerFactory}

import scala.annotation.static

class WoofLoggerFactory extends ILoggerFactory:
  def getLogger(s: String): Logger = WoofLogger(s)
end WoofLoggerFactory

class StaticLoggerBinder extends LoggerFactoryBinder:

  def getLoggerFactory(): org.slf4j.ILoggerFactory = StaticLoggerBinder.factory
  def getLoggerFactoryClassStr(): String           = classOf[WoofLoggerFactory].getName

end StaticLoggerBinder

object StaticLoggerBinder:
  @static val REQUESTED_API_VERSION: String    = "2.0.0"
  @static val SINGLETON: StaticLoggerBinder    = StaticLoggerBinder()
  @static def getSingleton: StaticLoggerBinder = SINGLETON
  lazy val factory                             = WoofLoggerFactory()

end StaticLoggerBinder
