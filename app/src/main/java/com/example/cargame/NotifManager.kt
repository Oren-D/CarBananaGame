package com.example.cargame

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast

class NotifManager private constructor(context: Context) {

    private val context: Context = context.applicationContext

    companion object {
        @Volatile
        private var instance: NotifManager? = null

        fun init(context: Context): NotifManager {
            return instance ?: synchronized(this) {
                instance ?: NotifManager(context).also { instance = it }
            }
        }

        fun getInstance(): NotifManager {
            return instance ?: throw IllegalStateException("NotifManager must be initialized by calling init(context) before use.")
        }
    }

    fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}