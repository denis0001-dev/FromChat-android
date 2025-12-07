package ru.fromchat

import android.app.Application
import com.pr0gramm3r101.utils.UtilsLibrary

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        UtilsLibrary.init(this)
    }
}