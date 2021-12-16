package org.legogroup.woof

import cats.kernel.Order

enum LogLevel:
  case Trace, Debug, Info, Warn, Error
given Order[LogLevel] = (x, y) => Order[Int].compare(x.ordinal, y.ordinal)
