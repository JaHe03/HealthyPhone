package com.example.healthyphone

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(
    context: Context,
    private val onStateChanged: (Boolean, Boolean, Boolean) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val stepDetector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var isBadPosture = false
    private var isTooDark = false
    private var isWalking = false

    private var lastStepTimestamp: Long = 0

    fun startListening() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        stepDetector?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        var stateChanged = false

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            lastStepTimestamp = System.currentTimeMillis()
            if (!isWalking) {
                isWalking = true
                stateChanged = true
            }
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val y = event.values[1]

            val newPostureState = y < 3.0f
            if (newPostureState != isBadPosture) {
                isBadPosture = newPostureState
                stateChanged = true
            }

            if (isWalking && (System.currentTimeMillis() - lastStepTimestamp > 3000)) {
                isWalking = false
                stateChanged = true
            }
        }

        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            val newLightState = lux < 10.0f
            if (newLightState != isTooDark) {
                isTooDark = newLightState
                stateChanged = true
            }
        }

        if (stateChanged) {
            onStateChanged(isBadPosture, isTooDark, isWalking)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}