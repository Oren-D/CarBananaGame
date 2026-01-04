package com.example.cargame

data class Score(
    val score: Int,
    val date: Long,
    val lat: Double,
    val lon: Double
) : Comparable<Score> {
    override fun compareTo(other: Score): Int {
        return other.score.compareTo(this.score)
    }
}