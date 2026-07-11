package com.tta.qrcodetoyota

import android.os.Bundle
import android.widget.TextView
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var vhalStatusTextView: TextView
    private lateinit var absTextView: TextView
    private lateinit var fuelTextView: TextView
    private lateinit var vinTextView: TextView
    private lateinit var vhalReader: VhalReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vhalStatusTextView = findViewById(R.id.vhal_status_text)
        absTextView = findViewById(R.id.abs_text)
        fuelTextView = findViewById(R.id.fuel_text)
        vinTextView = findViewById(R.id.vin_text)

        FileLogger.init(this)

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

    override fun onDestroy() {
        super.onDestroy()
        vhalReader.disconnect()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
