package com.example.cargame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cargame.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "CarBananaGame"

        binding.btnStartFast.setOnClickListener {
            startGame(isSensor = false, isSlow = false)
        }

        binding.btnStartSlow.setOnClickListener {
            startGame(isSensor = false, isSlow = true)
        }

        binding.btnStartSensor.setOnClickListener {
            startGame(isSensor = true, isSlow = false)
        }

        binding.btnHighScores.setOnClickListener {
            val intent = Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startGame(isSensor: Boolean, isSlow: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("SENSOR_MODE", isSensor)
        intent.putExtra("SLOW_MODE", isSlow)
        startActivity(intent)
    }
}