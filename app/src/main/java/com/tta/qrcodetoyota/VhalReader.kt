package com.tta.qrcodetoyota

import android.car.Car
import android.car.VehicleAreaType
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log

class VhalReader(
    private val context: Context,
    private val onAbsChanged: (String) -> Unit,
    private val onFuelChanged: (String) -> Unit,
    private val onVinChanged: (String) -> Unit
) : CarPropertyManager.CarPropertyEventCallback {

    private val ABS_IS_ACTIVE = 287310858
    private val FUEL_LEVEL = 291504903
    private val VIN = 286261504
    private var carPropertyManager: CarPropertyManager? = null
    private var car: Car? = null

    fun connect() {
        FileLogger.d(TAG, "Connecting to Car API...")
        try {
            car = Car.createCar(context)
            FileLogger.d(TAG, "Car instance created: $car")

            if (car != null && car!!.isConnected) {
                carPropertyManager = car!!.getCarManager(Car.PROPERTY_SERVICE) as? CarPropertyManager
                FileLogger.d(TAG, "CarPropertyManager obtained: $carPropertyManager")

                if (carPropertyManager != null) {
                    FileLogger.d(TAG, "Connected to Car API successfully")
                    registerCallbacks()
                } else {
                    val msg = "Failed to get CarPropertyManager"
                    FileLogger.e(TAG, msg)
                }
            }
        } catch (e: Exception) {
            val msg = "Error connecting to Car API: ${e.message}"
            FileLogger.e(TAG, msg, e)
        }
    }

    private fun registerCallbacks() {
        // Register ABS callback
        try {
            FileLogger.d(TAG, "Registering callback for ABS property (ID: $ABS_IS_ACTIVE)...")
            carPropertyManager?.registerCallback(this, ABS_IS_ACTIVE, 0f)
            FileLogger.d(TAG, "ABS Callback registered successfully")
        } catch (e: SecurityException) {
            FileLogger.permissionDenied(TAG, "android.car.permission.CAR_DYNAMICS (ABS_IS_ACTIVE)")
            FileLogger.exception(TAG, "ABS property registration", e)
            onAbsChanged("No Permission")
        } catch (e: Exception) {
            FileLogger.exception(TAG, "ABS property registration", e)
            onAbsChanged("Error")
        }

        // Register Fuel Level callback
        try {
            FileLogger.d(TAG, "Registering callback for Fuel Level property (ID: $FUEL_LEVEL)...")
            carPropertyManager?.registerCallback(this, FUEL_LEVEL, 0f)
            FileLogger.d(TAG, "Fuel Level Callback registered successfully")
        } catch (e: SecurityException) {
            FileLogger.permissionDenied(TAG, "android.car.permission.CAR_DYNAMICS (FUEL_LEVEL)")
            FileLogger.exception(TAG, "Fuel Level property registration", e)
            onFuelChanged("No Permission")
        } catch (e: Exception) {
            FileLogger.exception(TAG, "Fuel Level property registration", e)
            onFuelChanged("Error")
        }

        // Register VIN callback (static property)
        try {
            FileLogger.d(TAG, "Registering callback for VIN property (ID: $VIN)...")
            carPropertyManager?.registerCallback(this, VIN, 0f)
            FileLogger.d(TAG, "VIN Callback registered successfully")
        } catch (e: SecurityException) {
            FileLogger.permissionDenied(TAG, "android.car.permission.CAR_IDENTIFICATION (VIN)")
            FileLogger.exception(TAG, "VIN property registration", e)
            onVinChanged("No Permission")
        } catch (e: Exception) {
            FileLogger.exception(TAG, "VIN property registration", e)
            onVinChanged("Error")
        }
    }

    override fun onChangeEvent(carPropertyValue: CarPropertyValue<*>) {
        try {
            FileLogger.d(TAG, "Property changed! ID: ${carPropertyValue.propertyId}, Value: ${carPropertyValue.value}")

            when (carPropertyValue.propertyId) {
                ABS_IS_ACTIVE -> {
                    val absActive = carPropertyValue.value as? Boolean ?: false
                    val status = if (absActive) "ON" else "OFF"
                    FileLogger.d(TAG, "ABS Status: $status")
                    onAbsChanged(status)
                }
                FUEL_LEVEL -> {
                    val fuelLevel = carPropertyValue.value as? Int ?: 0
                    FileLogger.d(TAG, "Fuel Level: $fuelLevel mm")
                    onFuelChanged("$fuelLevel mm")
                }
                VIN -> {
                    val vin = carPropertyValue.value as? String ?: "N/A"
                    FileLogger.d(TAG, "VIN: $vin")
                    onVinChanged(vin)
                }
            }
        } catch (e: Exception) {
            val msg = "Error in onChangeEvent: ${e.message}"
            FileLogger.e(TAG, msg, e)
        }
    }

    override fun onErrorEvent(propertyId: Int, zone: Int) {
        val msg = "Error event - propertyId: $propertyId, zone: $zone"
        FileLogger.e(TAG, msg)
    }

    fun disconnect() {
        try {
            FileLogger.d(TAG, "Unregistering callbacks...")
            carPropertyManager?.unregisterCallback(this, ABS_IS_ACTIVE)
            carPropertyManager?.unregisterCallback(this, FUEL_LEVEL)
            carPropertyManager?.unregisterCallback(this, VIN)
            car?.disconnect()
            FileLogger.d(TAG, "Disconnected")
        } catch (e: Exception) {
            val msg = "Error disconnecting: ${e.message}"
            FileLogger.e(TAG, msg, e)
        }
    }

    companion object {
        private const val TAG = "VhalReader"
    }
}
