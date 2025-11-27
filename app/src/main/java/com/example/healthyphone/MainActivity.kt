package com.example.healthyphone

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var txtWalking: TextView
    private lateinit var txtLight: TextView
    private lateinit var txtPosture: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    // Receiver to handle data coming from the Service
    private val sensorReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "HEALTHY_PHONE_UPDATE") {
                val isWalking = intent.getBooleanExtra("walking", false)
                val isDark = intent.getBooleanExtra("dark", false)
                val isBadPosture = intent.getBooleanExtra("posture", false)

                updateUI(isWalking, isDark, isBadPosture)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Find Views
        txtWalking = findViewById(R.id.txtWalking)
        txtLight = findViewById(R.id.txtLight)
        txtPosture = findViewById(R.id.txtPosture)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        // 2. Setup Buttons
        btnStart.setOnClickListener {
            checkPermissionsAndStart()
        }

        btnStop.setOnClickListener {
            stopService()
            resetUI()
        }

        // 3. Register Receiver (Listen for updates)
        val filter = IntentFilter("HEALTHY_PHONE_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sensorReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(sensorReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop listening when app is closed to prevent crashes
        unregisterReceiver(sensorReceiver)
    }

    private fun updateUI(walking: Boolean, dark: Boolean, posture: Boolean) {
        txtWalking.text = if (walking) "Walking: YES" else "Walking: No"
        txtWalking.setTextColor(if (walking) 0xFFFF0000.toInt() else 0xFF333333.toInt()) // Red if walking

        txtLight.text = if (dark) "Light: TOO DARK" else "Light: Good"
        txtPosture.text = if (posture) "Posture: BAD" else "Posture: Good"
    }

    private fun resetUI() {
        txtWalking.text = "Walking: --"
        txtLight.text = "Light: --"
        txtPosture.text = "Posture: --"
    }

    private fun stopService() {
        val intent = Intent(this, HealthyService::class.java)
        stopService(intent)
    }

    // --- PERMISSION LOGIC (Same as before) ---
    private fun checkPermissionsAndStart() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 101)
        } else {
            startService()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startService()
        }
    }

    private fun startService() {
        val intent = Intent(this, HealthyService::class.java)
        startForegroundService(intent)
    }
}