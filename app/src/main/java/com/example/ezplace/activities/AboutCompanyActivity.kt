package com.example.ezplace.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.example.ezplace.R
import com.example.ezplace.models.Company
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_about_company.*
import kotlinx.android.synthetic.main.activity_new_company_details.*
import java.text.SimpleDateFormat
import java.util.*

class AboutCompanyActivity : BaseActivity() {

    lateinit var company : Company

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_company)

        setupActionBar(toolbar_about_company)

        company = intent.getParcelableExtra<Company>(Constants.COMPANY_DETAIL)!!

        setCompanyDetailsInUI()
    }

    private fun setCompanyDetailsInUI() {
        tv_cgpa_cut_off.text = company.cgpaCutOff.toString()
        val backlogs = if(company.backLogsAllowed ==1){
            "Allowed"
        } else{
            "Not allowed"
        }
        tv_backlogs.text = backlogs

        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, company.branchesAllowed)
        lv_branches.adapter = arrayAdapter

        tv_ctc.text = company.ctcDetails
        tv_location.text = company.location
        tv_job_profile.text = company.jobProfile

        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        tv_deadline_top_apply.text = sdf.format(company.deadlineToApply)
    }
}