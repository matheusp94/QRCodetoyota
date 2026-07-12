package com.tta.qrcodetoyota

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var vhalStatusTextView: TextView
    private lateinit var absTextView: TextView
    private lateinit var fuelTextView: TextView
    private lateinit var vinTextView: TextView
    private lateinit var logsDisplayTextView: TextView
    private lateinit var vhalReader: VhalReader
    private val logLines = mutableListOf<String>()
    private val maxLogLines = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vhalStatusTextView = findViewById(R.id.vhal_status_text)
        absTextView = findViewById(R.id.abs_text)
        fuelTextView = findViewById(R.id.fuel_text)
        vinTextView = findViewById(R.id.vin_text)
        logsDisplayTextView = findViewById(R.id.logs_display)

        FileLogger.init(this)

        // Iniciar captura de logs
        LogCapturer.start { logLine ->
            runOnUiThread {
                addLogLine(logLine)
            }
        }

        // Botão de copiar logs
        findViewById<android.widget.Button>(R.id.copy_logs_button).setOnClickListener {
            copyLogsToClipboard()
        }

        // Botão de refresh
        findViewById<android.widget.Button>(R.id.refresh_button).setOnClickListener {
            refreshVhal()
        }

        // Botão de fechar
        findViewById<android.widget.Button>(R.id.close_button).setOnClickListener {
            finish()
        }

        // Criar VhalReader com callbacks para ABS e Fuel
        vhalReader = VhalReader(
            this,
            { absStatus ->
                runOnUiThread {
                    absTextView.text = "ABS Status: $absStatus"
                    Log.d(TAG, "ABS updated: $absStatus")
                }
            },
            { fuelStatus ->
                runOnUiThread {
                    fuelTextView.text = "Fuel Level: $fuelStatus"
                    Log.d(TAG, "Fuel updated: $fuelStatus")
                }
            },
            { vinStatus ->
                runOnUiThread {
                    vinTextView.text = "VIN: $vinStatus"
                    Log.d(TAG, "VIN updated: $vinStatus")
                }
            }
        )

        Log.d(TAG, "MainActivity created")

        // Conectar e registrar callbacks
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
                    absTextView.text = "ABS Status: Error"
                    fuelTextView.text = "Fuel Level: Error"
                    vinTextView.text = "VIN: Error"
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
                    absTextView.text = "ABS Status: --"
                    fuelTextView.text = "Fuel Level: --"
                    vinTextView.text = "VIN: --"
                }

                vhalReader.disconnect()
                Thread.sleep(500)

                vhalReader.connect()

                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Connected"
                }

                FileLogger.d(TAG, "Refresh completed successfully")
            } catch (e: Exception) {
                val msg = "Error during refresh: ${e.message}"
                FileLogger.e(TAG, msg, e)
                runOnUiThread {
                    vhalStatusTextView.text = "VHAL: Refresh Error"
                }
            }
        }.start()
    }

    private fun addLogLine(line: String) {
        logLines.add(0, line)
        if (logLines.size > maxLogLines) {
            logLines.removeAt(logLines.size - 1)
        }
        logsDisplayTextView.text = logLines.joinToString("\n")
    }

    private fun copyLogsToClipboard() {
        val allLogs = logLines.asReversed().joinToString("\n")
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = android.content.ClipData.newPlainText("Logs", allLogs)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Logs copiados para área de transferência!", Toast.LENGTH_SHORT).show()
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
