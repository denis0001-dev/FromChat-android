package com.pr0gramm3r101.utils.settings

import platform.Foundation.NSUserDefaults

// TODO fix
class IosSettings : Settings {
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun putString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    override fun getString(key: String, default: String): String {
        return defaults.stringForKey(key) ?: default
    }

    override fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), key)
    }

    override fun getInt(key: String, default: Int): Int {
        return defaults.integerForKey(key).toInt()
    }

    override fun putLong(key: String, value: Long) {
        defaults.setObject(value, key)
    }

    override fun getLong(key: String, default: Long): Long {
        return defaults.objectForKey(key) as? Long ?: default
    }

    override fun putFloat(key: String, value: Float) {
        defaults.setFloat(value, key)
    }

    override fun getFloat(key: String, default: Float): Float {
        return defaults.floatForKey(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        return defaults.boolForKey(key)
    }

    override fun putStringSet(key: String, value: Set<String>) {
        defaults.setObject(value.toList(), key)
    }

    override fun getStringSet(key: String, default: Set<String>): Set<String> {
        val list = defaults.objectForKey(key) as? List<*> ?: return default
        return list.filterIsInstance<String>().toSet()
    }

    override fun putStringList(key: String, value: List<String>) {
        defaults.setObject(value, key)
    }

    override fun getStringList(key: String, default: List<String>): List<String> {
        val list = defaults.objectForKey(key) as? List<*> ?: return default
        return list.filterIsInstance<String>()
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
} 