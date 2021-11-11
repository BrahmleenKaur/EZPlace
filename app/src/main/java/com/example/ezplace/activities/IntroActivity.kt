package com.example.ezplace.activities

import android.content.Intent
import android.os.Bundle
import com.example.ezplace.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        fullScreenMode()
        customFont(tv_app_name_intro)

        btn_sign_in_intro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        btn_sign_up_intro.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }
}