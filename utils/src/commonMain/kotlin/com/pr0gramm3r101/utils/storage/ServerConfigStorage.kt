package com.pr0gramm3r101.utils.storage

/**
 * Server configuration data
 */
data class ServerConfigData(
    val serverUrl: String,
    val httpsEnabled: Boolean
)

/**
 * Server configuration storage
 */
object ServerConfigStorage {
    private val storage = NamespacedStorage("server_config")
    
    private const val SERVER_URL_KEY = "server_url"
    private const val HTTPS_ENABLED_KEY = "https_enabled"
    
    suspend fun getServerUrl(): String? {
        val url = storage.getString(SERVER_URL_KEY)
        return if (url.isEmpty()) null else url
    }
    
    suspend fun setServerUrl(url: String) {
        storage.putString(SERVER_URL_KEY, url)
    }
    
    suspend fun getHttpsEnabled(): Boolean? {
        return if (storage.contains(HTTPS_ENABLED_KEY)) {
            storage.getBoolean(HTTPS_ENABLED_KEY, true)
        } else {
            null
        }
    }
    
    suspend fun setHttpsEnabled(enabled: Boolean) {
        storage.putBoolean(HTTPS_ENABLED_KEY, enabled)
    }
    
    suspend fun hasConfiguration(): Boolean {
        return getServerUrl() != null
    }
    
    suspend fun getConfig(): ServerConfigData {
        val url = getServerUrl() ?: "fromchat.ru"
        val https = getHttpsEnabled() ?: true
        return ServerConfigData(url, https)
    }
    
    suspend fun saveConfig(config: ServerConfigData) {
        setServerUrl(config.serverUrl)
        setHttpsEnabled(config.httpsEnabled)
    }
}
