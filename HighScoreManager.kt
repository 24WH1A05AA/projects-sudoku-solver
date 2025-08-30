package com.example.sudokusolver

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HighScoreManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("sudoku_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveScore(score: Int) {
        val scores = getScores().toMutableList()
        scores += score
        scores.sortDescending()
        val top = scores.take(5)
        prefs.edit().putString("leaderboard", gson.toJson(top)).apply()
    }

    fun getScores(): List<Int> {
        val json = prefs.getString("leaderboard", null) ?: return emptyList()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type)
    }
}
