package com.pr0gramm3r101.utils.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CommonCrypto.CCHmac
import platform.CommonCrypto.kCCHmacAlgSHA256

actual object Hmac {
    actual suspend fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray = withContext(Dispatchers.Default) {
        val result = ByteArray(32)
        
        key.usePinned { keyPinned ->
            data.usePinned { dataPinned ->
                result.usePinned { resultPinned ->
                    CCHmac(
                        algorithm = kCCHmacAlgSHA256,
                        key = keyPinned.addressOf(0),
                        keyLength = key.size.toULong(),
                        data = dataPinned.addressOf(0),
                        dataLength = data.size.toULong(),
                        macOut = resultPinned.addressOf(0)
                    )
                }
            }
        }
        
        result
    }
}
