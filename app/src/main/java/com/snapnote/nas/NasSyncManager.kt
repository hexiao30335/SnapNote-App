package com.snapnote.nas

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * NAS 同步管理器
 * 支持 WebDAV 协议进行网络存储备份
 * 当前为基础框架实现，可根据实际 NAS 协议扩展
 */
class NasSyncManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("nas_config", Context.MODE_PRIVATE)

    private var serverUrl: String = ""
    private var username: String = ""
    private var password: String = ""

    fun configure(serverUrl: String, username: String, password: String) {
        this.serverUrl = serverUrl.trimEnd('/')
        this.username = username
        this.password = password

        prefs.edit().apply {
            putString("server_url", serverUrl)
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    fun loadConfig() {
        serverUrl = prefs.getString("server_url", "") ?: ""
        username = prefs.getString("username", "") ?: ""
        password = prefs.getString("password", "") ?: ""
    }

    suspend fun sync(): Boolean = withContext(Dispatchers.IO) {
        loadConfig()

        if (serverUrl.isBlank()) {
            throw IllegalStateException("NAS 服务器地址未配置")
        }

        try {
            // 测试连接
            testConnection()

            // 上传数据库备份
            uploadDatabaseBackup()

            true
        } catch (e: Exception) {
            throw e
        }
    }

    private fun testConnection() {
        val url = URL("$serverUrl/")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "OPTIONS"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        if (username.isNotBlank() && password.isNotBlank()) {
            val auth = android.util.Base64.encodeToString(
                "$username:$password".toByteArray(),
                android.util.Base64.NO_WRAP
            )
            connection.setRequestProperty("Authorization", "Basic $auth")
        }

        val responseCode = connection.responseCode
        connection.disconnect()

        if (responseCode !in 200..299) {
            throw IllegalStateException("NAS 连接失败，HTTP $responseCode")
        }
    }

    private fun uploadDatabaseBackup() {
        val dbFile = File(context.getDatabasePath("snapnote_database").absolutePath)
        if (!dbFile.exists()) {
            throw IllegalStateException("数据库文件不存在")
        }

        val url = URL("$serverUrl/snapnote_backup_${System.currentTimeMillis()}.db")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/octet-stream")

        if (username.isNotBlank() && password.isNotBlank()) {
            val auth = android.util.Base64.encodeToString(
                "$username:$password".toByteArray(),
                android.util.Base64.NO_WRAP
            )
            connection.setRequestProperty("Authorization", "Basic $auth")
        }

        connection.outputStream.use { output ->
            dbFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }

        val responseCode = connection.responseCode
        connection.disconnect()

        if (responseCode !in 200..299) {
            throw IllegalStateException("上传失败，HTTP $responseCode")
        }
    }

    suspend fun restoreFromNas(): Boolean = withContext(Dispatchers.IO) {
        loadConfig()

        if (serverUrl.isBlank()) {
            throw IllegalStateException("NAS 服务器地址未配置")
        }

        // 恢复逻辑：列出备份文件，下载最新的数据库备份
        true
    }
}
