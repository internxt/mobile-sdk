package com.internxt.mobilesdk.services.crypto


import org.spongycastle.crypto.PBEParametersGenerator
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.generators.PKCS5S2ParametersGenerator
import org.spongycastle.crypto.params.KeyParameter


class KeyDerivation {
  fun pbkdf2(password: String, salt: String, rounds: Int, derivedKeyLength: Int): ByteArray {
    val generator = PKCS5S2ParametersGenerator(SHA512Digest())
    generator.init(password.toByteArray(Charsets.UTF_8), salt.toByteArray(Charsets.UTF_8), rounds)
    val keyParameter = generator.generateDerivedParameters(derivedKeyLength * 8) as KeyParameter

    return keyParameter.key
  }
}
