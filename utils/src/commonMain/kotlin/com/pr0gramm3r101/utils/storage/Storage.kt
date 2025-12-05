package com.pr0gramm3r101.utils.storage

import com.pr0gramm3r101.utils.settings.AsyncSettings
import com.pr0gramm3r101.utils.settings.asyncSettings

/**
 * Generic key-value storage using AsyncSettings
 */
class Storage(private val settings: AsyncSettings = asyncSettings) {
    suspend fun putString(key: String, value: String) = settings.putString(key, value)
    suspend fun getString(key: String, default: String = ""): String = settings.getString(key, default)
    
    suspend fun putInt(key: String, value: Int) = settings.putInt(key, value)
    suspend fun getInt(key: String, default: Int = 0): Int = settings.getInt(key, default)
    
    suspend fun putLong(key: String, value: Long) = settings.putLong(key, value)
    suspend fun getLong(key: String, default: Long = 0L): Long = settings.getLong(key, default)
    
    suspend fun putFloat(key: String, value: Float) = settings.putFloat(key, value)
    suspend fun getFloat(key: String, default: Float = 0f): Float = settings.getFloat(key, default)
    
    suspend fun putBoolean(key: String, value: Boolean) = settings.putBoolean(key, value)
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean = settings.getBoolean(key, default)
    
    suspend fun putStringSet(key: String, value: Set<String>) = settings.putStringSet(key, value)
    suspend fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String> = settings.getStringSet(key, default)
    
    suspend fun putStringList(key: String, value: List<String>) = settings.putStringList(key, value)
    suspend fun getStringList(key: String, default: List<String> = emptyList()): List<String> = settings.getStringList(key, default)
    
    suspend fun remove(key: String) = settings.remove(key)
    suspend fun contains(key: String): Boolean = settings.contains(key)
    suspend fun clear() = settings.clear()
}

/**
 * Namespaced storage for specific features
 */
class NamespacedStorage(private val prefix: String, private val storage: Storage = Storage()) {
    private fun key(name: String) = "$prefix:$name"
    
    suspend fun putString(name: String, value: String) = storage.putString(key(name), value)
    suspend fun getString(name: String, default: String = ""): String = storage.getString(key(name), default)
    
    suspend fun putInt(name: String, value: Int) = storage.putInt(key(name), value)
    suspend fun getInt(name: String, default: Int = 0): Int = storage.getInt(key(name), default)
    
    suspend fun putLong(name: String, value: Long) = storage.putLong(key(name), value)
    suspend fun getLong(name: String, default: Long = 0L): Long = storage.getLong(key(name), default)
    
    suspend fun putFloat(name: String, value: Float) = storage.putFloat(key(name), value)
    suspend fun getFloat(name: String, default: Float = 0f): Float = storage.getFloat(key(name), default)
    
    suspend fun putBoolean(name: String, value: Boolean) = storage.putBoolean(key(name), value)
    suspend fun getBoolean(name: String, default: Boolean = false): Boolean = storage.getBoolean(key(name), default)
    
    suspend fun remove(name: String) = storage.remove(key(name))
    suspend fun contains(name: String): Boolean = storage.contains(key(name))
}
