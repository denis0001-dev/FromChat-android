package com.pr0gramm3r101.utils.settings

actual val settings: Settings get() = IosSettings()
actual val secureSettings: Settings
    get() = TODO("Not yet implemented")