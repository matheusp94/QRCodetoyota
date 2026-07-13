package com.tta.qrcodetoyota

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var vhalStatusTextView: TextView
    private lateinit var absTextView: TextView
    private lateinit var fuelTextView: TextView
    private lateinit var vinTextView: TextView
    private lateinit var vhalReader: VhalReader
    private val systemLogs = mutableListOf<String>()
    private val maxSystemLogs = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vhalStatusTextView = findViewById(R.id.vhal_status_text)
        absTextView = findViewById(R.id.abs_text)
        fuelTextView = findViewById(R.id.fuel_text)
        vinTextView = findViewById(R.id.vin_text)

        FileLogger.init(this)

        // Start capturing system Logcat (to get CarPropertyManager errors, etc)
        LogCapturer.start { logLine ->
            synchronized(systemLogs) {
                systemLogs.add(logLine)
                if (systemLogs.size > maxSystemLogs) {
                    systemLogs.removeAt(0)
                }
            }
        }

        // Copy logs button
        findViewById<android.widget.Button>(R.id.copy_logs_button).setOnClickListener {
            copyLogsToClipboard()
        }

        // Refresh button
        findViewById<android.widget.Button>(R.id.refresh_button).setOnClickListener {
            refreshVhal()
        }

        // Close button
        findViewById<android.widget.Button>(R.id.close_button).setOnClickListener {
            finish()
        }

        // Create VhalReader with callbacks for ABS and Fuel
        vhalReader = VhalReader(
            this,
            { absStatus ->
                runOnUiThread {
                    absTextView.text = absStatus
                    Log.d(TAG, "ABS updated: $absStatus")
                }
            },
            { fuelStatus ->
                runOnUiThread {
                    fuelTextView.text = fuelStatus
                    Log.d(TAG, "Fuel updated: $fuelStatus")
                }
            },
            { vinStatus ->
                runOnUiThread {
                    vinTextView.text = vinStatus
                    Log.d(TAG, "VIN updated: $vinStatus")
                }
            }
        )

        Log.d(TAG, "MainActivity created")

        // Connect and register callbacks
        Thread {
            try {
                Log.d(TAG, "Attempting to connect to VHAL...")
                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Connecting..."
                }

                vhalReader.connect()

                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Connected"
                }

                Log.d(TAG, "Connected and listening for property changes...")
            } catch (e: Exception) {
                Log.e(TAG, "Error connecting to VHAL", e)
                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Connection Error"
                    absTextView.text = "Error"
                    fuelTextView.text = "Error"
                    vinTextView.text = "Error"
                }
            }
        }.start()
    }

    private fun refreshVhal() {
        FileLogger.d(TAG, "Refresh button clicked")
        Thread {
            try {
                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Refreshing..."
                    absTextView.text = "Refreshing..."
                    fuelTextView.text = "Refreshing..."
                    vinTextView.text = "Refreshing..."
                }

                vhalReader.disconnect()
                Thread.sleep(500)

                vhalReader.connect()

                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Connected"
                }

                // Wait for callbacks to fire (data arrival)
                Thread.sleep(2000)

                runOnUiThread {
                    // If still showing "Refreshing...", it means no data arrived
                    if (absTextView.text.toString().contains("Refreshing")) {
                        absTextView.text = "ABS Status: N/A"
                    }
                    if (fuelTextView.text.toString().contains("Refreshing")) {
                        fuelTextView.text = "Fuel Level: N/A"
                    }
                    if (vinTextView.text.toString().contains("Refreshing")) {
                        vinTextView.text = "VIN: N/A"
                    }
                }

                FileLogger.d(TAG, "Refresh completed successfully")
            } catch (e: Exception) {
                val msg = "Error during refresh: ${e.message}"
                FileLogger.e(TAG, msg, e)
                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Refresh Error"
                    absTextView.text = "Error"
                    fuelTextView.text = "Error"
                    vinTextView.text = "Error"
                }
            }
        }.start()
    }

    private fun copyLogsToClipboard() {
        try {
            val logFile = java.io.File(filesDir, "logs/app.log")

            // Get app logs
            val appLogs = if (logFile.exists()) logFile.readText() else ""

            // Get system logs
            val systemLogsText = synchronized(systemLogs) {
                systemLogs.joinToString("\n")
            }

            // Combine both
            val allLogs = "$appLogs\n\n=== System Logcat (CarPropertyManager, Permissions, etc) ===\n$systemLogsText"

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Logs", allLogs)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Complete logs copied to clipboard!", Toast.LENGTH_SHORT).show()
            FileLogger.d(TAG, "Logs copied to clipboard")
        } catch (e: Exception) {
            FileLogger.exception(TAG, "Copy logs to clipboard failed", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogCapturer.stop()
        vhalReader.disconnect()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
