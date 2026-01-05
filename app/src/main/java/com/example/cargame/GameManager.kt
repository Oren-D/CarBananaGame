package com.example.cargame

import kotlin.random.Random

class GameManager {

    companion object {
        const val GRID_ROWS = 5
        const val GRID_COLS = 5
        const val INITIAL_LIVES = 3
    }

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
    private var lives: Int = INITIAL_LIVES
    private var lane: Int = 2
    private var counter: Int = 0
    val lMatrix = Array(GRID_ROWS) { IntArray(GRID_COLS) { 0 } }

    fun setGameListener(listener: GameListener) {
        this.listener = listener
    }

    fun moveCar(direction: Int) {
        val newLane = (lane + direction).coerceIn(0, GRID_COLS - 1)
        if (lane != newLane) {
            lane = newLane
            listener?.onLaneChange(lane)
        }
        checkCollision()
    }

    fun timerTick() {
        for (i in GRID_ROWS - 1 downTo 1) {
            for (j in 0 until GRID_COLS) {
                lMatrix[i][j] = lMatrix[i - 1][j]
            }
        }

        for (j in 0 until GRID_COLS) lMatrix[0][j] = 0
        if (counter % 2 == 0) {
            val randomLane = Random.nextInt(GRID_COLS)
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
        val item = lMatrix[GRID_ROWS - 1][lane]
        if (item == 1) {
            lMatrix[GRID_ROWS - 1][lane] = 0
            lives--
            listener?.onLivesUpdate(lives)
            if (lives <= 0) {
                listener?.onGameOver(score)
            }
        } else if (item == 2) {
            lMatrix[GRID_ROWS - 1][lane] = 0
            score += 100
            listener?.onCoinCollected()
            listener?.onScoreUpdate(score)
        }
    }

    fun resetGame() {
        lives = INITIAL_LIVES
        score = 0
        lane = 2
        counter = 0
        for (i in 0 until GRID_ROWS) {
            for (j in 0 until GRID_COLS) lMatrix[i][j] = 0
        }
        listener?.onLaneChange(lane)
        listener?.onLivesUpdate(lives)
        listener?.onScoreUpdate(score)
        listener?.onGridUpdate(lMatrix)
    }
}
