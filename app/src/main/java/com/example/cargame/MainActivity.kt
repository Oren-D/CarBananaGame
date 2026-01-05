package com.example.cargame

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.cargame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SensorEventListener, GameManager.GameListener {

    companion object {
        const val GAME_SCORE_KEY = "GAME_SCORE_KEY"
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var gameManager: GameManager
    private var isGameRunning: Boolean = false

    private var isSensorMode: Boolean = false
    private lateinit var sensorManager: SensorManager
    private var accSensor: Sensor? = null
    private var lastSensorMoveTime: Long = 0

    private lateinit var viewsArray: Array<Array<ImageView>>
    private lateinit var cars: Array<ImageView>

    private val handler: Handler = Handler(Looper.getMainLooper())
    private var runnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, gameManager.delay)
            gameManager.timerTick()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotifManager.init(this)

        val isSlow = intent.getBooleanExtra("SLOW_MODE", false)
        isSensorMode = intent.getBooleanExtra("SENSOR_MODE", false)

        gameManager = GameManager()
        gameManager.setGameListener(this)
        gameManager.delay = if (isSlow) 1000 else 500

        initViews()
        initSensors()

        binding.btnLeft.setOnClickListener { gameManager.moveCar(-1) }
        binding.btnRight.setOnClickListener { gameManager.moveCar(1) }

        if (isSensorMode) {
            binding.btnLeft.visibility = View.INVISIBLE
            binding.btnRight.visibility = View.INVISIBLE
        }

        startGame()
    }

    private fun initSensors() {
        if (isSensorMode) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
    }

    private fun initViews() {
        viewsArray = arrayOf(
            arrayOf(binding.rock00, binding.rock01, binding.rock02, binding.rock03, binding.rock04),
            arrayOf(binding.rock10, binding.rock11, binding.rock12, binding.rock13, binding.rock14),
            arrayOf(binding.rock20, binding.rock21, binding.rock22, binding.rock23, binding.rock24),
            arrayOf(binding.rock30, binding.rock31, binding.rock32, binding.rock33, binding.rock34),
            arrayOf(binding.rock40, binding.rock41, binding.rock42, binding.rock43, binding.rock44)
        )

        cars = arrayOf(binding.car0, binding.car1, binding.car2, binding.car3, binding.car4)
    }

    override fun onResume() {
        super.onResume()
        if (isSensorMode && accSensor != null) {
            sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorMode) {
            sensorManager.unregisterListener(this)
        }
        stopTimer()
    }

    override fun onStart() {
        super.onStart()
        if (isGameRunning) {
            startTimer()
        }
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val x = event.values[0]
        val y = event.values[1]

        if (System.currentTimeMillis() - lastSensorMoveTime > 300) {
            if (x > 3.0) {
                gameManager.moveCar(-1)
                lastSensorMoveTime = System.currentTimeMillis()
            } else if (x < -3.0) {
                gameManager.moveCar(1)
                lastSensorMoveTime = System.currentTimeMillis()
            }
        }

        if (y < -2.0) {
            gameManager.delay = 300
        } else if (y > 4.0) {
            gameManager.delay = 900
        } else {
            val isSlow = intent.getBooleanExtra("SLOW_MODE", false)
            gameManager.delay = if (isSlow) 1000 else 500
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startTimer() {
        stopTimer()
        handler.post(runnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable)
    }

    private fun startGame() {
        isGameRunning = true
        gameManager.resetGame()
        startTimer()
    }

    private fun updateGridUI(lMatrix: Array<IntArray>) {
        for (i in 0..4) {
            for (j in 0..4) {
                val value = lMatrix[i][j]
                val view = viewsArray[i][j]

                if (value == 1) {
                    view.visibility = View.VISIBLE
                    view.setImageResource(R.drawable.bannana)
                } else if (value == 2) {
                    view.visibility = View.VISIBLE
                    view.setImageResource(R.drawable.coin)
                } else {
                    view.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun onScoreUpdate(score: Int) {
        binding.scoreLbl.text = String.format("%05d m", score)
    }

    override fun onLivesUpdate(lives: Int) {
        binding.life1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        binding.life2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        binding.life3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE

        if (lives < 3 && isGameRunning) {
            NotifManager.getInstance().toast("Noob!")
            NotifManager.getInstance().vibrate()
            val mediaPlayer = MediaPlayer.create(this, R.raw.crash)
            mediaPlayer.start()
        }
    }

    override fun onGridUpdate(grid: Array<IntArray>) {
        updateGridUI(grid)
    }

    override fun onGameOver(score: Int) {
        isGameRunning = false
        stopTimer()
        NotifManager.getInstance().toast("You Sucked")

        handler.postDelayed({
            val intent = Intent(this, ScoreActivity::class.java)
            intent.putExtra(GAME_SCORE_KEY, score)
            startActivity(intent)
            finish()
        }, 1500)
    }

    override fun onLaneChange(newLane: Int) {
        for (i in cars.indices) {
            cars[i].visibility = if (i == newLane) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onCoinCollected() {
        NotifManager.getInstance().toast("100 points!")
    }
}
