package com.pr0gramm3r101.utils.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSUserDefaults

class IosAsyncSettings : AsyncSettings {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.Default) {
        defaults.setObject(value, key)
        defaults.synchronize()
    }
    
    override suspend fun getString(key: String, default: String): String = withContext(Dispatchers.Default) {
        defaults.stringForKey(key) ?: default
    }
    
    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.Default) {
        defaults.setInteger(value.toLong(), key)
        defaults.synchronize()
    }
    
    override suspend fun getInt(key: String, default: Int): Int = withContext(Dispatchers.Default) {
        val value = defaults.integerForKey(key)
        if (value != null) value.toInt() else default
    }
    
    override suspend fun putLong(key: String, value: Long) = withContext(Dispatchers.Default) {
        defaults.setObject(value, key)
        defaults.synchronize()
    }
    
    override suspend fun getLong(key: String, default: Long): Long = withContext(Dispatchers.Default) {
        (defaults.objectForKey(key) as? platform.Foundation.NSNumber)?.longValue ?: default
    }
    
    override suspend fun putFloat(key: String, value: Float) = withContext(Dispatchers.Default) {
        defaults.setFloat(value, key)
        defaults.synchronize()
    }
    
    override suspend fun getFloat(key: String, default: Float): Float = withContext(Dispatchers.Default) {
        val value = defaults.floatForKey(key)
        if (value != null) value.toFloat() else default
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) = withContext(Dispatchers.Default) {
        defaults.setBool(value, key)
        defaults.synchronize()
    }
    
    override suspend fun getBoolean(key: String, default: Boolean): Boolean = withContext(Dispatchers.Default) {
        if (defaults.objectForKey(key) != null) {
            defaults.boolForKey(key)
        } else {
            default
        }
    }
    
    override suspend fun putStringSet(key: String, value: Set<String>) = withContext(Dispatchers.Default) {
        defaults.setObject(value.toList(), key)
        defaults.synchronize()
    }
    
    override suspend fun getStringSet(key: String, default: Set<String>): Set<String> = withContext(Dispatchers.Default) {
        val list = defaults.objectForKey(key) as? List<*> ?: return@withContext default
        list.filterIsInstance<String>().toSet()
    }
    
    override suspend fun putStringList(key: String, value: List<String>) = withContext(Dispatchers.Default) {
        defaults.setObject(value, key)
        defaults.synchronize()
    }
    
    override suspend fun getStringList(key: String, default: List<String>): List<String> = withContext(Dispatchers.Default) {
        val list = defaults.objectForKey(key) as? List<*> ?: return@withContext default
        list.filterIsInstance<String>()
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.Default) {
        defaults.removeObjectForKey(key)
        defaults.synchronize()
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.Default) {
        defaults.objectForKey(key) != null
    }
    
    override suspend fun clear() = withContext(Dispatchers.Default) {
        val domain = defaults.persistentDomainForName(defaults.bundleIdentifier() ?: "")
        domain?.keys?.forEach { key ->
            defaults.removeObjectForKey(key as String)
        }
        defaults.synchronize()
    }
}

actual val asyncSettings: AsyncSettings = IosAsyncSettings()
