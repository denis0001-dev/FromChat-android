package com.pr0gramm3r101.utils.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.pr0gramm3r101.utils.UtilsLibrary

/**
 * Singleton DataStore instance to avoid multiple DataStore instances for the same file
 */
object DataStoreSingleton {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    
    val dataStore: DataStore<Preferences>
        get() = UtilsLibrary.context.dataStore
}
