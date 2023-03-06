package org.legogroup.woof.slf4j2

import org.slf4j.ILoggerFactory
import org.slf4j.helpers.NOP_FallbackServiceProvider

class WoofLoggerServiceProvider extends NOP_FallbackServiceProvider:
  override def getLoggerFactory: ILoggerFactory = (name: String) => new WoofLogger(name)
end WoofLoggerServiceProvider