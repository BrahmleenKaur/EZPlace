package com.example.ezplace.activities

import android.os.Build
import android.os.Bundle
import android.provider.SyncStateContract
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.ezplace.R
import com.example.ezplace.firebase.FirebaseAuthClass
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.College
import com.example.ezplace.models.Company
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_new_company_details.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_update_profile.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class NewCompanyDetailsActivity : BaseActivity() {

    private val branches = arrayOf(
        "Computer Science", "Electronics and Communication", "Electrical",
        "Instrumentation and Control", "Mechanical", "Textile"
    )

    lateinit var companiesNamesAndIds: HashMap<String, String>

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_company_details)

        setupActionBar(toolbar_new_company)

        addBranchesListInLayout()

        btn_submit_new_company.setOnClickListener {
            // Call a function to add new company in the database.
            submitNewCompanyDetails()
        }
    }

    private fun addBranchesListInLayout() {
        val checkboxLinearLayout = ll_check_boxes

        for (i in branches.indices) {
            val checkBox = CheckBox(this)
            checkBox.text = branches[i]
            checkboxLinearLayout.addView(checkBox)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun submitNewCompanyDetails() {
        // Here we get the text from editText and trim the space
        val companyName: String = autoCompanyNameTextView.text.toString().trim { it <= ' ' }
        val cgpaCutOffString: String = et_cgpa_cut_off.text.toString().trim { it <= ' ' }
        val ctcDetails = et_ctc_details.text.toString().trim { it <= ' ' }
        val companyLocation: String = et_location_new_company.text.toString().trim { it <= ' ' }
        val deadlineToApply: String = et_deadline_to_apply.text.toString().trim { it <= ' ' }
        val backLogsAllowed: Boolean = rb_backlogs_allowed.isSelected

        var branchesAllowed: ArrayList<String> = ArrayList<String>()

        for (i in 0 until ll_check_boxes.childCount) {
            val checkBox: CheckBox = ll_check_boxes.getChildAt(i) as CheckBox
            if (checkBox.isChecked) {
                Log.i("branch", checkBox.text.toString())
                branchesAllowed.add(checkBox.text.toString())
            }
        }

        if (validateForm(
                companyName,
                cgpaCutOffString,
                branchesAllowed,
                ctcDetails,
                companyLocation,
                deadlineToApply
            )
        ) {
            val cgpaCutOff = cgpaCutOffString.toDouble()
            var company = Company(
                companyName, cgpaCutOff,
                backLogsAllowed, branchesAllowed, ctcDetails, companyLocation, deadlineToApply
            )
            registerCompanyInCollegeDatabase(company)
        }
    }

    private fun registerCompanyInCollegeDatabase(company: Company) {
        showProgressDialog(resources.getString(R.string.please_wait))
        var tpoId : String= FirebaseAuthClass().getCurrentUserID()

        //First we retrieve collegeCode from tpoId and from that function ,we'll call addCompanyInCollege()
        FirestoreClass().getCollegeCode(company,tpoId,this)
    }

    fun companyRegisteredSuccess(){
        hideProgressDialog()
        Toast.makeText(this, getString(R.string.company_is_added),
            Toast.LENGTH_LONG).show()
        finish()
    }

    /**
     * A function to validate the details of a new company.
     */

    private fun validateForm(
        companyName: String,
        cgpaCutOff: String,
        branchesAllowed: ArrayList<String>,
        ctcDetails: String,
        companyLocation: String,
        deadlineToApply: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(companyName) -> {
                showErrorSnackBar(getString(R.string.enter_company_name))
                false
            }
            TextUtils.isEmpty(cgpaCutOff) -> {
                showErrorSnackBar(getString(R.string.enter_cgpa_cut_off))
                false
            }
            rg_backlogs.checkedRadioButtonId == -1 -> {
                showErrorSnackBar(getString(R.string.enter_backlogs_details))
                false
            }
            branchesAllowed.size == 0 -> {
                showErrorSnackBar(getString(R.string.enter_at_least_one_branch))
                false
            }
            TextUtils.isEmpty(ctcDetails) -> {
                showErrorSnackBar(getString(R.string.enter_ctc_details))
                false
            }
            TextUtils.isEmpty(companyLocation) -> {
                showErrorSnackBar(getString(R.string.enter_company_location))
                false
            }
            TextUtils.isEmpty(deadlineToApply) -> {
                showErrorSnackBar(getString(R.string.enter_deadline_to_apply))
                false
            }
            else -> {
                true
            }
        }
    }
}
