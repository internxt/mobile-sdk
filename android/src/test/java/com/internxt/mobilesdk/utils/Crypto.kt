package com.internxt.mobilesdk.utils

import com.internxt.mobilesdk.services.crypto.Hash
import org.junit.jupiter.api.Test
import org.spongycastle.jcajce.provider.digest.RIPEMD160
import java.security.KeyStore
import java.security.Security

internal class CryptoUtilsTest {

  private val SUT: CryptoUtils = CryptoUtils()

  @Test
  fun `Should list available providers`() {
    SUT.getAvailableAlgorithms()
  }
}
