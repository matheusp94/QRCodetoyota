package com.tta.qrcodetoyota

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileLogger {
    private var logFile: File? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    fun init(context: Context) {
        try {
            val logsDir = File(context.filesDir, "logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            logFile = File(logsDir, "app.log")
            if (!logFile!!.exists()) {
                logFile!!.createNewFile()
            }
            writeLog("INFO", "MAIN", "=== App Started ===")
        } catch (e: Exception) {
            Log.e("FileLogger", "Error initializing FileLogger: ${e.message}", e)
        }
    }

    fun d(tag: String, message: String) {
        writeLog("DEBUG", tag, message)
        Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        writeLog("INFO", tag, message)
        Log.i(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val fullMsg = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        writeLog("WARN", tag, fullMsg)
        Log.w(tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMsg = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        writeLog("ERROR", tag, fullMsg)
        Log.e(tag, message, throwable)
    }

    private fun writeLog(level: String, tag: String, message: String) {
        try {
            if (logFile == null) return

            val timestamp = dateFormat.format(Date())
            val logLine = "[$timestamp] $level/$tag: $message\n"

            logFile!!.appendText(logLine)
        } catch (e: Exception) {
            Log.e("FileLogger", "Error writing to log file: ${e.message}")
        }
    }

    fun getLogFilePath(context: Context): String {
        return File(context.filesDir, "logs/app.log").absolutePath
    }
}
