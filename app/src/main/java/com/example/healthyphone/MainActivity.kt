package com.example.healthyphone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var sensorHandler: SensorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sensorHandler = SensorHandler(this)
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.startListening()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.stopListening()
    }
}