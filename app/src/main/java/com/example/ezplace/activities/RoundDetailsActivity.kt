package com.example.ezplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ezplace.R
import com.example.ezplace.models.Company
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_round_details.*

class RoundDetailsActivity : BaseActivity() {

    lateinit var company : Company
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round_details)

        setupActionBar(toolbar_round_details)

        company = intent.getParcelableExtra<Company>(Constants.COMPANY_DETAIL)!!

        fab_about_company.setOnClickListener {
            val intent = Intent(this, AboutCompanyActivity::class.java)
            intent.putExtra(Constants.COMPANY_DETAIL, company)
//                    intent.putExtra(Constants.STUDENT_DETAIL, mStudent)
//                    intent.putExtra(Constants.BYADMIN, isAdminHere)
            startActivity(intent)
        }
    }
}