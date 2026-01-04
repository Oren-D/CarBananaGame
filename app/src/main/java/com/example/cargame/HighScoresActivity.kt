package com.example.cargame

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HighScoresActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.high_scores)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_list, HighScoresListFragment())
                .replace(R.id.frame_map, MapFragment())
                .commit()
        }

        findViewById<ImageButton>(R.id.btnBackToMenu).setOnClickListener {
            finish()
        }
    }
}