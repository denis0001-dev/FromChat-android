package ru.fromchat.core

import android.util.Log

actual object Logger {
    actual fun d(tag: String, message: String, throwable: Throwable?) {
        Log.d(tag, message, throwable)
    }

    actual fun i(tag: String, message: String, throwable: Throwable?) {
        Log.i(tag, message, throwable)
    }

    actual fun w(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}
