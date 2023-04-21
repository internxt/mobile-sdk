package com.internxt.mobilesdk.utils

import java.util.Date

object Performance {
  fun measureTime(): TimeMeasurer {
    return TimeMeasurer()
  }
}


class TimeMeasurer {
  val start: Date = Date()

  fun getMs(): Long {
    val now = Date()
    return now.time - start.time
  }
}
