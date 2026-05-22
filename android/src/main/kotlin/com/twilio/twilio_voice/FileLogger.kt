package com.twilio.twilio_voice

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class FileLogger(private val context: Context) {
    companion object {
        private const val TAG = "FileLogger"
        private var instance: FileLogger? = null
        
        fun getInstance(context: Context): FileLogger {
            if (instance == null) {
                instance = FileLogger(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val logDir: File by lazy {
        val dir = File(context.getExternalFilesDir(null), "logs")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    
    private val currentLogFile: File
        get() {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fileName = "twilio_voice_${dateFormat.format(Date())}.log"
            return File(logDir, fileName)
        }
    
    fun log(tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val logMessage = buildString {
            append("$timestamp $tag: $message")
            if (throwable != null) {
                append("\n")
                append(throwable.stackTraceToString())
            }
            append("\n")
        }
        
        try {
            FileWriter(currentLogFile, true).use { writer ->
                writer.write(logMessage)
                writer.flush()
            }
            
            // Also keep console log for debug builds
            if (BuildConfig.DEBUG) {
                android.util.Log.d(tag, message, throwable)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to write to log file", e)
        }
    }
    
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        log(tag, "ERROR: $message", throwable)
    }
    
    fun logWarning(tag: String, message: String) {
        log(tag, "WARNING: $message")
    }
    
    fun getLogFiles(): List<File> {
        return logDir.listFiles()?.filter { it.extension == "log" } ?: emptyList()
    }
    
    fun getLogContent(file: File): String {
        return file.readText()
    }
    
    fun clearOldLogs(daysToKeep: Int = 7) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        getLogFiles().forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
}