package com.pr0gramm3r101.utils.settings

import com.pr0gramm3r101.utils.UtilsLibrary

actual val settings: Settings get() = AndroidSettings(UtilsLibrary.context)