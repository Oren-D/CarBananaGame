package com.example.cargame

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ScoreActivity : AppCompatActivity() {

    private lateinit var scoreResultLbl: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        ScoreManager.init(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        scoreResultLbl = findViewById(R.id.scoreResultLbl)
        score = intent.getIntExtra(MainActivity.GAME_SCORE_KEY, 0)
        scoreResultLbl.text = "Score: $score"

        val btnScore = findViewById<ImageButton>(R.id.btnScore)
        btnScore.setOnClickListener {
            val intent = Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
            finish()
        }

        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else {
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                ScoreManager.getInstance().saveScore(score, 0.0, 0.0)
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    ScoreManager.getInstance().saveScore(score, location.latitude, location.longitude)
                } else {
                    ScoreManager.getInstance().saveScore(score, 0.0, 0.0)
                }
            }
        }
    }
}