package com.example.cloudstine

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity(R.layout.activity_splash) {

    companion object {
        const val SPLASH_SCREEN_TIME_MS = 500L
    }

    private val handler = Handler(Looper.getMainLooper())

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed({ startMainActivity() }, SPLASH_SCREEN_TIME_MS)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onPause() {
        handler.removeCallbacksAndMessages(null)
        super.onPause()
    }
}