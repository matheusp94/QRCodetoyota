package com.tta.qrcodetoyota

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object LogCapturer {
    private var onLogLine: ((String) -> Unit)? = null
    private var captureThread: Thread? = null
    private var isCapturing = false

    fun start(onLogLine: (String) -> Unit) {
        if (isCapturing) return

        this.onLogLine = onLogLine
        isCapturing = true

        captureThread = Thread {
            try {
                val process = Runtime.getRuntime().exec("logcat -v threadtime")
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                var line = reader.readLine()
                while (isCapturing && line != null) {
                    onLogLine(line)
                    line = reader.readLine()
                }

                reader.close()
                process.destroy()
            } catch (e: Exception) {
                Log.e("LogCapturer", "Error capturing logs: ${e.message}", e)
            }
        }

        captureThread?.start()
    }

    fun stop() {
        isCapturing = false
        captureThread?.interrupt()
    }
}
