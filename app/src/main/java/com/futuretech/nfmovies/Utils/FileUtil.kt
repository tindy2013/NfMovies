package com.futuretech.nfmovies.Utils

import android.content.Context
import android.os.Build
import android.util.Log

import java.io.*
import java.nio.charset.StandardCharsets

object FileUtil {
    fun write(path: String, filename: String, content: String): Boolean {
        var file = File(path)
        if (!file.exists()) file.mkdirs()
        try {
            file = File(path + filename)
            val outputStream = FileOutputStream(file)
            outputStream.write(content.toByteArray())
            outputStream.flush()
            outputStream.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

    fun getFromRaw(context: Context, id: Int): String {
        var result = ""
        try {
            val `in` = context.resources.openRawResource(id)
            val length = `in`.available()
            val buffer = ByteArray(length)
            `in`.read(buffer)
            result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String(buffer, StandardCharsets.UTF_8)
            } else {
                String(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }

    fun deleteDirectory(path: File): Boolean {
        Log.i("Deleted ", path.absolutePath)
        if (path.exists()) {
            val files = path.listFiles() ?: return false
            for (file in files) {
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    val wasSuccessful = file.delete()
                    if (wasSuccessful) {
                        Log.i("Deleted ", "successfully")
                    }
                }
            }
        }
        return path.delete()
    }
}
