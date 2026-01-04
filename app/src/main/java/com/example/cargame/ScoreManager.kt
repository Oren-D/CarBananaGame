package com.example.cargame

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ScoreManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("GAME_SCORES", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        @Volatile
        private var instance: ScoreManager? = null

        fun init(context: Context): ScoreManager {
            return instance ?: synchronized(this) {
                instance ?: ScoreManager(context).also { instance = it }
            }
        }

        fun getInstance(): ScoreManager {
            return instance ?: throw IllegalStateException("ScoreManager must be initialized first")
        }
    }

    fun saveScore(score: Int, lat: Double, lon: Double) {
        val scores = getTop10Scores().toMutableList()
        scores.add(Score(score, System.currentTimeMillis(), lat, lon))

        scores.sort()
        val top10 = scores.take(10)

        val json = gson.toJson(top10)
        sharedPreferences.edit().putString("SCORES_DATA_JSON", json).apply()
    }

    fun getTop10Scores(): List<Score> {
        val json = sharedPreferences.getString("SCORES_DATA_JSON", null) ?: return emptyList()
        val type = object : TypeToken<List<Score>>() {}.type
        return gson.fromJson(json, type)
    }
}