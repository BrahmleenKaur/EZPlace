package com.example.ezplace.activities

import android.os.Bundle
import com.example.ezplace.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        fullScreenMode()
        customFont(tv_app_name_intro)
    }
}