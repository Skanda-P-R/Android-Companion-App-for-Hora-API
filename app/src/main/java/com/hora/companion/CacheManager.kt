package com.hora.companion

import android.content.Context
import java.io.File

class CacheManager(private val context: Context) {
    private val dir: File = context.filesDir

    fun saveJson(name: String, json: String) {
        val file = File(dir, name)
        file.writeText(json)
    }

    fun readJson(name: String): String? {
        val file = File(dir, name)
        return if (file.exists()) file.readText() else null
    }

    fun saveBytes(name: String, bytes: ByteArray) {
        val file = File(dir, name)
        file.writeBytes(bytes)
    }

    fun readBytes(name: String): ByteArray? {
        val file = File(dir, name)
        return if (file.exists()) file.readBytes() else null
    }

    fun lastModified(name: String): Long {
        val file = File(dir, name)
        return if (file.exists()) file.lastModified() else 0L
    }
}
