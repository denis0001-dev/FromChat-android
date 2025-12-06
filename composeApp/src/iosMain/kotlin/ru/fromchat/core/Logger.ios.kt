package ru.fromchat.core

import platform.Foundation.NSLog

actual object Logger {
    actual fun d(tag: String, message: String, throwable: Throwable?) {
        NSLog("DEBUG: [%s] %s %s", tag, message, throwable?.message ?: "")
    }

    actual fun i(tag: String, message: String, throwable: Throwable?) {
        NSLog("INFO: [%s] %s %s", tag, message, throwable?.message ?: "")
    }

    actual fun w(tag: String, message: String, throwable: Throwable?) {
        NSLog("WARN: [%s] %s %s", tag, message, throwable?.message ?: "")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        NSLog("ERROR: [%s] %s %s", tag, message, throwable?.message ?: "")
    }
}

