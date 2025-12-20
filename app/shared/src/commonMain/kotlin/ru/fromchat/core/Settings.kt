package ru.fromchat.core

import ru.fromchat.ui.Theme
import com.pr0gramm3r101.utils.settings.Settings

object Settings {
    private val settings = Settings.Companion()

    var materialYou: Boolean
        get() = settings.getBoolean("materialYou", true)
        set(value) = settings.putBoolean("materialYou", value)

    var theme: Theme
        get() = Theme.entries[settings.getInt("theme", Theme.AsSystem.ordinal)]
        set(value) = settings.putInt("theme", value.ordinal)
}

