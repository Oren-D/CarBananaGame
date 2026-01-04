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
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        const val GAME_SCORE_KEY = "GAME_SCORE_KEY"
    }

    private lateinit var binding: ActivityMainBinding

    var DELAY: Long = 700
    var counter: Int = 0
    var score: Int = 0
    var lives: Int = 3
    var lane: Int = 2
    var isGameRunning: Boolean = false

    private var isSensorMode: Boolean = false
    private lateinit var sensorManager: SensorManager
    private var accSensor: Sensor? = null
    private var lastSensorMoveTime: Long = 0

    val lMatrix = Array(5) { IntArray(5) { 0 } }

    private lateinit var viewsArray: Array<Array<ImageView>>
    private lateinit var cars: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotifManager.init(this)

        val isSlow = intent.getBooleanExtra("SLOW_MODE", false)
        isSensorMode = intent.getBooleanExtra("SENSOR_MODE", false)

        if (isSlow) {
            DELAY = 1000
        } else {
            DELAY = 500
        }

        initViews()
        initSensors()

        binding.btnLeft.setOnClickListener { moveCar(-1) }
        binding.btnRight.setOnClickListener { moveCar(1) }

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
                moveCar(-1)
                lastSensorMoveTime = System.currentTimeMillis()
            } else if (x < -3.0) {
                moveCar(1)
                lastSensorMoveTime = System.currentTimeMillis()
            }
        }

        if (y < -2.0) {
            DELAY = 300
        } else if (y > 4.0) {
            DELAY = 900
        } else {
            val isSlow = intent.getBooleanExtra("SLOW_MODE", false)
            DELAY = if (isSlow) 1000 else 500
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    val handler: Handler = Handler(Looper.getMainLooper())

    var runnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, DELAY)
            timerTask()
        }
    }

    fun startTimer() {
        stopTimer()
        handler.post(runnable)
    }

    fun stopTimer() {
        handler.removeCallbacks(runnable)
    }

    private fun startGame() {
        isGameRunning = true
        resetGameData()
        startTimer()
    }

    private fun resetGameData() {
        lives = 3
        counter = 0
        score = 0
        lane = 2

        for (i in 0..4) {
            for (j in 0..4) lMatrix[i][j] = 0
        }

        updateScoreUI()
        updateLivesUI()
        updateGridUI()

        for (c in cars) c.visibility = View.INVISIBLE
        cars[lane].visibility = View.VISIBLE
    }

    fun timerTask() {
        for (i in 4 downTo 1) {
            for (j in 0..4) {
                lMatrix[i][j] = lMatrix[i - 1][j]
            }
        }

        for (j in 0..4) lMatrix[0][j] = 0

        if (counter % 2 == 0) {
            val lane = Random.nextInt(5)
            val isCoin = Random.nextInt(100) < 30
            if (isCoin) {
                lMatrix[0][lane] = 2
            } else {
                lMatrix[0][lane] = 1
            }
        }
        counter++

        score += 10

        updateScoreUI()
        updateGridUI()
        checkCollision()
    }

    private fun moveCar(direction: Int) {
        if (!isGameRunning) return

        cars[lane].visibility = View.INVISIBLE
        lane += direction

        if (lane < 0) lane = 0
        if (lane > 4) lane = 4

        cars[lane].visibility = View.VISIBLE
        checkCollision()
    }

    private fun checkCollision() {
        val item = lMatrix[4][lane]

        if (item == 1) {
            lMatrix[4][lane] = 0
            lives--
            updateLivesUI()
            NotifManager.getInstance().toast("Noob!")
            NotifManager.getInstance().vibrate()

            val mediaPlayer = MediaPlayer.create(this, R.raw.crash)
            mediaPlayer.start()

            if (lives <= 0) handleGameOver()

        } else if (item == 2) {
            lMatrix[4][lane] = 0
            score += 100
            NotifManager.getInstance().toast("Bing!")
            updateScoreUI()
        }
    }

    private fun handleGameOver() {
        stopTimer()
        NotifManager.getInstance().toast("You Sucked")

        handler.postDelayed({
            val intent = Intent(this, ScoreActivity::class.java)
            intent.putExtra(GAME_SCORE_KEY, score)
            startActivity(intent)
            finish()
        }, 1500)
    }

    private fun updateGridUI() {
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

    private fun updateScoreUI() {
        binding.scoreLbl.text = String.format("%05d m", score)
    }

    private fun updateLivesUI() {
        binding.life1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        binding.life2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        binding.life3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }
}