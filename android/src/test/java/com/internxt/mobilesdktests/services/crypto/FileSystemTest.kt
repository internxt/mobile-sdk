package com.internxt.mobilesdktests.services.crypto

import com.internxt.mobilesdk.services.FS
import com.internxt.mobilesdk.utils.Logger
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileSystemTest {
  private val SUT = FS

  @Test
  fun `should get the name from a path`() {

    val filename = SUT.getFilenameFromPath("/path/test_file_123.pdf")
    println("Filename $filename")
    assertEquals(filename, "test_file_123")
  }
}
