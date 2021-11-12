package com.example.ezplace.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.ezplace.R
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class IntroActivity : BaseActivity() {

    lateinit var isStudent : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        fullScreenMode()
        customFont(tv_app_name_intro)

        btn_sign_up_tpo_intro.setOnClickListener {
            introToSignUpIntent(false)
        }

        btn_sign_up_student_intro.setOnClickListener {
            introToSignUpIntent(true)
        }

        tv_sign_in.setOnClickListener{
            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }
    }

    fun introToSignUpIntent(value : Boolean){
        intent = Intent(this, SignUpActivity::class.java)
        intent.putExtra(Constants.IS_STUDENT, value)
        startActivity(intent)
    }
}