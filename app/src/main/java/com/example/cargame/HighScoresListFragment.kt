package com.example.cargame

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.gson.Gson

class HighScoresListFragment : Fragment() {

    private lateinit var scoresContainer: LinearLayout
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_high_scores_list, container, false)
        scoresContainer = view.findViewById(R.id.scores_container)

        ScoreManager.init(requireContext().applicationContext)
        updateList()

        return view
    }

    private fun updateList() {
        val topScores = ScoreManager.getInstance().getTop10Scores()
        scoresContainer.removeAllViews()

        for ((index, scoreObj) in topScores.withIndex()) {
            val button = Button(requireContext())
            button.text = "${index + 1}. Score: ${scoreObj.score}"
            button.textSize = 18f

            button.setOnClickListener {
                val json = gson.toJson(scoreObj)
                val bundle = Bundle()
                bundle.putString("SCORE_DATA", json)
                parentFragmentManager.setFragmentResult("SCORE_CHANNEL", bundle)
            }

            scoresContainer.addView(button)
        }
    }
}