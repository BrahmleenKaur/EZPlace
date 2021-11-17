package com.example.ezplace.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ezplace.R
import kotlinx.android.synthetic.main.activity_round_details.*

class RoundDetailsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round_details)

        setupActionBar(toolbar_round_details)
    }
}