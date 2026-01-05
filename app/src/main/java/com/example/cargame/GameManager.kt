package com.example.cargame

import kotlin.random.Random

class GameManager {

    interface GameListener {
        fun onScoreUpdate(score: Int)
        fun onLivesUpdate(lives: Int)
        fun onGridUpdate(grid: Array<IntArray>)
        fun onGameOver(score: Int)
        fun onLaneChange(newLane: Int)
        fun onCoinCollected()
    }

    private var listener: GameListener? = null

    var delay: Long = 700
    private var score: Int = 0
    private var lives: Int = 3
    private var lane: Int = 2
    private var counter: Int = 0
    val lMatrix = Array(5) { IntArray(5) { 0 } }

    fun setGameListener(listener: GameListener) {
        this.listener = listener
    }

    fun moveCar(direction: Int) {
        val newLane = (lane + direction).coerceIn(0, 4)
        if (lane != newLane) {
            lane = newLane
            listener?.onLaneChange(lane)
        }
        checkCollision()
    }

    fun timerTick() {
        // Shift rocks down
        for (i in 4 downTo 1) {
            for (j in 0..4) {
                lMatrix[i][j] = lMatrix[i - 1][j]
            }
        }

        
        for (j in 0..4) lMatrix[0][j] = 0
        if (counter % 2 == 0) {
            val randomLane = Random.nextInt(5)
            val isCoin = Random.nextInt(100) < 25
            lMatrix[0][randomLane] = if (isCoin) 2 else 1
        }
        counter++

        score += 10
        listener?.onScoreUpdate(score)
        listener?.onGridUpdate(lMatrix)
        checkCollision()
    }

    private fun checkCollision() {
        val item = lMatrix[4][lane]
        if (item == 1) { 
            lMatrix[4][lane] = 0
            lives--
            listener?.onLivesUpdate(lives)
            if (lives <= 0) {
                listener?.onGameOver(score)
            }
        } else if (item == 2) { 
            lMatrix[4][lane] = 0
            score += 100
            listener?.onCoinCollected()
            listener?.onScoreUpdate(score)
        }
    }

    fun resetGame() {
        lives = 3
        score = 0
        lane = 2
        counter = 0
        for (i in 0..4) {
            for (j in 0..4) lMatrix[i][j] = 0
        }
        listener?.onLaneChange(lane)
        listener?.onLivesUpdate(lives)
        listener?.onScoreUpdate(score)
        listener?.onGridUpdate(lMatrix)
    }
}
