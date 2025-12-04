package com.example.healthyphone

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(
    context: Context,
    private val onSensorUpdate: (Boolean, Boolean, Boolean, String?) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val stepDetector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private var isBadPosture = false
    private var isTooDark = false
    private var isWalking = false

    private var postureStartTime: Long = 0
    private var lightStartTime: Long = 0
    private var stepCount = 0
    private var lastStepTime: Long = 0

    private var postureAlertSent = false
    private var lightAlertSent = false
    private var walkingAlertSent = false

    private val TIME_THRESHOLD = 5000L
    private val STEP_THRESHOLD = 10

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
        var shouldUpdateUI = false
        var alertMessage: String? = null
        val now = System.currentTimeMillis()

        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            if (now - lastStepTime > 3000) {
                stepCount = 0
                walkingAlertSent = false
            }

            lastStepTime = now
            stepCount++

            if (!isWalking) {
                isWalking = true
                shouldUpdateUI = true
            }

            if (stepCount >= STEP_THRESHOLD && !walkingAlertSent) {
                alertMessage = "WALK_ALERT"
                walkingAlertSent = true
            }
        }

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val y = event.values[1]

            if (isWalking && (now - lastStepTime > 3000)) {
                isWalking = false
                stepCount = 0
                walkingAlertSent = false
                shouldUpdateUI = true
            }

            val newPostureState = y < 3.0f

            if (newPostureState) {
                if (postureStartTime == 0L) postureStartTime = now

                if ((now - postureStartTime > TIME_THRESHOLD) && !postureAlertSent) {
                    alertMessage = "POSTURE_ALERT"
                    postureAlertSent = true
                }
            } else {
                postureStartTime = 0L
                postureAlertSent = false
            }

            if (newPostureState != isBadPosture) {
                isBadPosture = newPostureState
                shouldUpdateUI = true
            }
        }

        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            val newLightState = lux < 10.0f

            if (newLightState) {
                if (lightStartTime == 0L) lightStartTime = now

                if ((now - lightStartTime > TIME_THRESHOLD) && !lightAlertSent) {
                    alertMessage = "LIGHT_ALERT"
                    lightAlertSent = true
                }
            } else {
                lightStartTime = 0L
                lightAlertSent = false
            }

            if (newLightState != isTooDark) {
                isTooDark = newLightState
                shouldUpdateUI = true
            }
        }

        if (shouldUpdateUI || alertMessage != null) {
            onSensorUpdate(isBadPosture, isTooDark, isWalking, alertMessage)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}