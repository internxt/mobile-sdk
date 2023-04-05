package com.internxt.mobilesdk.utils

class CryptoFunctionNotAvailableException(message: String) : Exception(message)
class ConfigValueMissingException(message: String) : Exception(message)
class NotSupportedEncryptModeException(message: String): Exception(message)
class InvalidMnemonicException(message: String): Exception(message)
class InvalidArgumentException(message: String): Exception(message)
class UrlNotReceivedFromNetworkException(): Exception()
class EmptyFileException(message: String): Exception(message)
class FileAccessRejectionException(message: String?) :
  Exception(message)
class ApiResponseException(message: String):Exception(message)

class DuplicatedUpload(message: String): Exception(message)
