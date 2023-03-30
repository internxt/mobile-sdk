package com.internxt.mobilesdk.core


enum class EncryptMode {
  AesCTRNoPadding
}

data class EncryptConfig(
  val mode: EncryptMode,
  val key: ByteArray,
  val iv: ByteArray
)

