package com.pr0gramm3r101.utils.settings

/**
 * Async version of Settings API using suspend functions
 */
interface AsyncSettings {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String, default: String = ""): String
    
    suspend fun putInt(key: String, value: Int)
    suspend fun getInt(key: String, default: Int = 0): Int
    
    suspend fun putLong(key: String, value: Long)
    suspend fun getLong(key: String, default: Long = 0L): Long
    
    suspend fun putFloat(key: String, value: Float)
    suspend fun getFloat(key: String, default: Float = 0f): Float
    
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, default: Boolean = false): Boolean
    
    suspend fun putStringSet(key: String, value: Set<String>)
    suspend fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String>
    
    suspend fun putStringList(key: String, value: List<String>)
    suspend fun getStringList(key: String, default: List<String> = emptyList()): List<String>
    
    suspend fun remove(key: String)
    
    suspend fun contains(key: String): Boolean
    suspend fun clear()
}

/**
 * Global async settings instance
 */
expect val asyncSettings: AsyncSettings
