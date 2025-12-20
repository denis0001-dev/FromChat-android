package com.pr0gramm3r101.utils.storage

/**
 * Authentication token storage
 */
object AuthStorage {
    private val storage = NamespacedStorage("auth")
    
    suspend fun getToken(): String? {
        val token = storage.getString("token")
        return token.ifEmpty { null }
    }
    
    suspend fun setToken(token: String?) {
        if (token != null) {
            storage.putString("token", token)
        } else {
            storage.remove("token")
        }
    }
    
    suspend fun clearToken() {
        storage.remove("token")
    }
    
    suspend fun putString(key: String, value: String) = storage.putString(key, value)
    suspend fun getString(key: String, default: String = ""): String = storage.getString(key, default)
    suspend fun remove(key: String) = storage.remove(key)
}
