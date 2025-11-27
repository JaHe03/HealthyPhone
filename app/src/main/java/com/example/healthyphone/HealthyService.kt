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

        sensorHandler = SensorHandler(this) { isBadPosture, isTooDark, isWalking ->

            val isScreenOn = powerManager.isInteractive

            notificationHelper.updateNotification(
                isBadPosture, isTooDark, isWalking, isScreenOn
            )
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