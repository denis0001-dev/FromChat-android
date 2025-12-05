package com.pr0gramm3r101.utils

import android.content.Context
import java.lang.ref.WeakReference

actual object UtilsLibrary {
    private var init = false
    private lateinit var _context: WeakReference<Context>
    internal val context get() = _context.get()!!

    actual fun init(vararg args: Any) {
        _context = WeakReference(args[0] as Context)
    }
}