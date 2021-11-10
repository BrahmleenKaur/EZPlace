package com.example.ezplace.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.ezplace.R
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        fullScreenMode()
        customFont(tv_app_name)

        // 3 SECOND DELAY BEFORE SWITCHING TO NEXT ACTIVITY
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
            finish()
        }, 300)
    }
}