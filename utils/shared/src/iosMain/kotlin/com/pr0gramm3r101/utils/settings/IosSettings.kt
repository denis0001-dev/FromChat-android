package com.pr0gramm3r101.utils.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

// TODO fix
class IosSettings : Settings {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override suspend fun putString(key: String, value: String) = defaults.setObject(value, key)

    override suspend fun getString(key: String, default: String) = defaults.stringForKey(key) ?: default

    override suspend fun putInt(key: String, value: Int) = defaults.setInteger(value.toLong(), key)

    override suspend fun getInt(key: String, default: Int) = defaults.integerForKey(key).toInt()

    override suspend fun putLong(key: String, value: Long) = defaults.setObject(value, key)

    override suspend fun getLong(key: String, default: Long) = defaults.objectForKey(key) as? Long ?: default

    override suspend fun putFloat(key: String, value: Float) = defaults.setFloat(value, key)

    override suspend fun getFloat(key: String, default: Float) = defaults.floatForKey(key)

    override suspend fun putBoolean(key: String, value: Boolean) = defaults.setBool(value, key)

    override suspend fun getBoolean(key: String, default: Boolean) = defaults.boolForKey(key)

    override suspend fun putStringSet(key: String, value: Set<String>) = defaults.setObject(value.toList(), key)

    override suspend fun getStringSet(key: String, default: Set<String>): Set<String> {
        return (defaults.objectForKey(key) as? List<*> ?: return default)
            .filterIsInstance<String>()
            .toSet()
    }

    override suspend fun putStringList(key: String, value: List<String>) = defaults.setObject(value, key)

    override suspend fun getStringList(key: String, default: List<String>): List<String> {
        return (defaults.objectForKey(key) as? List<*> ?: return default)
            .filterIsInstance<String>()
    }

    override suspend fun remove(key: String) = defaults.removeObjectForKey(key)

    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.Default) {
        defaults.objectForKey(key) != null
    }
} 