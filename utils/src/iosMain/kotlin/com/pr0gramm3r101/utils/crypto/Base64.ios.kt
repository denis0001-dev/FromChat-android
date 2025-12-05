package com.pr0gramm3r101.utils.crypto

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData

actual object Base64 {
    actual fun encode(bytes: ByteArray): String {
        return bytes.usePinned { pinned ->
            val nsData = NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
            nsData.base64EncodedStringWithOptions(0u)
        }
    }
    
    actual fun decode(base64: String): ByteArray {
        val nsData = NSData.create(base64EncodedString = base64, options = 0u) ?: return ByteArray(0)
        val length = nsData.length.toInt()
        val bytes = nsData.bytes ?: return ByteArray(0)
        return ByteArray(length) { index ->
            bytes.reinterpret<ByteVar>()[index]
        }
    }
}
