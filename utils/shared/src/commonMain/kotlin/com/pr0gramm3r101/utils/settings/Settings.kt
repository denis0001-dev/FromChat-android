@file:Suppress("NOTHING_TO_INLINE")

package com.pr0gramm3r101.utils.settings

interface Settings {
    companion object {
        inline fun get() = settings
        inline operator fun invoke() = settings
    }

    fun putString(key: String, value: String)
    fun getString(key: String, default: String = ""): String
    
    fun putInt(key: String, value: Int)
    fun getInt(key: String, default: Int = 0): Int
    
    fun putLong(key: String, value: Long)
    fun getLong(key: String, default: Long = 0L): Long
    
    fun putFloat(key: String, value: Float)
    fun getFloat(key: String, default: Float = 0f): Float
    
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    
    fun putStringSet(key: String, value: Set<String>)
    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String>
    
    fun putStringList(key: String, value: List<String>)
    fun getStringList(key: String, default: List<String> = emptyList()): List<String>

    fun remove(key: String)
} 