package com.pr0gramm3r101.utils.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class AndroidSettings(): Settings {
    private val dataStore: DataStore<Preferences>
        get() = DataStoreSingleton.dataStore

    override fun putString(key: String, value: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    override fun getString(key: String, default: String) = runBlocking {
        dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: default
        }.first()
    }

    override fun putInt(key: String, value: Int) = runBlocking {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
        Unit
    }

    override fun getInt(key: String, default: Int) = runBlocking {
        dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)] ?: default
        }.first()
    }

    override fun putLong(key: String, value: Long) = runBlocking {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
        Unit
    }

    override fun getLong(key: String, default: Long) = runBlocking {
        dataStore.data.map { preferences ->
            preferences[longPreferencesKey(key)] ?: default
        }.first()
    }

    override fun putFloat(key: String, value: Float) = runBlocking {
        dataStore.edit { preferences ->
            preferences[floatPreferencesKey(key)] = value
        }
        Unit
    }

    override fun getFloat(key: String, default: Float) = runBlocking {
        dataStore.data.map { preferences ->
            preferences[floatPreferencesKey(key)] ?: default
        }.first()
    }

    override fun putBoolean(key: String, value: Boolean) = runBlocking {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
        Unit
    }

    override fun getBoolean(key: String, default: Boolean) = runBlocking {
        dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: default
        }.first()
    }

    override fun putStringSet(key: String, value: Set<String>) = runBlocking {
        dataStore.edit { preferences ->
            preferences[stringSetPreferencesKey(key)] = value
        }
        Unit
    }

    override fun getStringSet(key: String, default: Set<String>) = runBlocking {
        dataStore.data.map { preferences ->
            preferences[stringSetPreferencesKey(key)] ?: default
        }.first()
    }

    override fun putStringList(key: String, value: List<String>) = putStringSet(key, value.toSet())

    override fun getStringList(key: String, default: List<String>) = getStringSet(key, default.toSet()).toList()

    override fun remove(key: String) = runBlocking {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(intPreferencesKey(key))
            preferences.remove(longPreferencesKey(key))
            preferences.remove(floatPreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
            preferences.remove(stringSetPreferencesKey(key))
        }
        Unit
    }
}