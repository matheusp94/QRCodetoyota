package com.tta.qrcodetoyota

import android.content.Context
import android.os.Build
import android.os.Process
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
            writeLog("DEBUG", "SYSTEM", "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            writeLog("DEBUG", "SYSTEM", "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        } catch (e: Exception) {
            Log.e("FileLogger", "Error initializing FileLogger: ${e.message}", e)
        }
    }

    fun d(tag: String, message: String) {
        writeLog("D", tag, message)
        Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        writeLog("I", tag, message)
        Log.i(tag, message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        val fullMsg = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        writeLog("W", tag, fullMsg)
        Log.w(tag, message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val fullMsg = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        writeLog("E", tag, fullMsg)
        Log.e(tag, message, throwable)
    }

    fun permissionDenied(tag: String, permission: String) {
        val message = "Permission denied: $permission"
        writeLog("W", tag, message)
        Log.w(tag, message)
    }

    fun permissionGranted(tag: String, permission: String) {
        val message = "Permission granted: $permission"
        writeLog("I", tag, message)
        Log.i(tag, message)
    }

    fun exception(tag: String, action: String, exception: Exception) {
        val fullMsg = "$action\nException: ${exception.javaClass.simpleName}\nMessage: ${exception.message}\n${exception.stackTraceToString()}"
        writeLog("E", tag, fullMsg)
        Log.e(tag, "$action: ${exception.message}", exception)
    }

    private fun writeLog(level: String, tag: String, message: String) {
        try {
            if (logFile == null) return

            val timestamp = dateFormat.format(Date())
            val pid = Process.myPid()
            val tid = Thread.currentThread().id
            val pidTid = "$pid-$tid"

            val logLine = "$timestamp  $pidTid  $tag  com.tta.qrcodetoyota  $level  $message\n"

            logFile!!.appendText(logLine)
        } catch (e: Exception) {
            Log.e("FileLogger", "Error writing to log file: ${e.message}")
        }
    }

    fun getLogFilePath(context: Context): String {
        return File(context.filesDir, "logs/app.log").absolutePath
    }
}
