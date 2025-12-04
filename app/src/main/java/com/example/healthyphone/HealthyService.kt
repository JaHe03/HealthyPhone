package com.example.healthyphone

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager

class HealthyService : Service() {

    private lateinit var sensorHandler: SensorHandler
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var powerManager: PowerManager

    override fun onCreate() {
        super.onCreate()

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        notificationHelper = NotificationHelper(this)

        sensorHandler = SensorHandler(this) { isBadPosture, isTooDark, isWalking, alertCode ->

            val isScreenOn = powerManager.isInteractive

            val statusText = when {
                isWalking && isScreenOn -> "Monitoring: Walking detected..."
                isBadPosture && isTooDark -> "Monitoring: Bad Posture & Dark Room"
                isBadPosture -> "Monitoring: Poor Posture detected..."
                isTooDark -> "Monitoring: Low light detected..."
                else -> "Monitoring: All Good."
            }
            notificationHelper.updatePersistentNotification(statusText)

            if (alertCode != null && isScreenOn) {

                if ((alertCode == "POSTURE_ALERT" || alertCode == "LIGHT_ALERT") && isBadPosture && isTooDark) {
                    notificationHelper.sendHighPriorityAlert(
                        "Double Health Warning",
                        "You have been sitting in low light and slouching for a while. Sit up and turn on a light."
                    )
                } else {
                    when (alertCode) {
                        "POSTURE_ALERT" -> {
                            notificationHelper.sendHighPriorityAlert(
                                "Posture Warning",
                                "You have been slouching for a while. Sit up!"
                            )
                        }
                        "LIGHT_ALERT" -> {
                            notificationHelper.sendHighPriorityAlert(
                                "Eye Strain Warning",
                                "It has been dark for too long. Turn on a light."
                            )
                        }
                        "WALK_ALERT" -> {
                            notificationHelper.sendHighPriorityAlert(
                                "Safety Alert",
                                "Do not text and walk! Look up."
                            )
                        }
                    }
                }
            }

            val intent = Intent("HEALTHY_PHONE_UPDATE")
            intent.putExtra("walking", isWalking)
            intent.putExtra("dark", isTooDark)
            intent.putExtra("posture", isBadPosture)
            intent.setPackage(packageName)
            sendBroadcast(intent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startNotification = notificationHelper.getBaseNotification()
        val notifId = notificationHelper.getNotificationId()

        startForeground(notifId, startNotification)
        sensorHandler.startListening()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorHandler.stopListening()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}