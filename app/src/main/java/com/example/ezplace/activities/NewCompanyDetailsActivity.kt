package com.example.ezplace.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.CompanyNameAndLastRound
import com.example.ezplace.models.Round
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_add_round.*
import kotlinx.android.synthetic.main.activity_new_company_details.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewCompanyDetailsActivity : BaseActivity() {

    lateinit var collegeCode: String
    private val myCalendar: Calendar = Calendar.getInstance()
    private lateinit var company : Company
    private var eligibleStudents = ArrayList<Student>()
    private var eligibleStudentsIds: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_company_details)
        setupActionBar(toolbar_new_company)

        /** Initialize college Code passed from previous activity i.e. Main activity */
        if (intent.hasExtra(Constants.COLLEGE_CODE))
            collegeCode = intent.getStringExtra(Constants.COLLEGE_CODE)!!

        addBranchesCheckboxesInLayout()

        /** for date picker dialog */
        val date =
            OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                et_deadline_to_apply.setText(dateLongToString(myCalendar))
            }

        /** when user clicks on editText view,
         * then datepicker dialog should open */
        et_deadline_to_apply.setOnClickListener {
            val myDatePicker = DatePickerDialog(
                this@NewCompanyDetailsActivity, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            myDatePicker.datePicker.minDate = (System.currentTimeMillis())
            myDatePicker.show()
        }

        btn_submit_new_company.setOnClickListener {
            /**Call a function to add new company in the database */
            submitNewCompanyDetails()
        }
    }

    /** Shows the list of branches in the layout */
    private fun addBranchesCheckboxesInLayout() {
        val checkboxLinearLayout = ll_check_boxes

        for (i in Constants.ALL_BRANCHES.indices) {
            val checkBox = CheckBox(this)
            checkBox.text = Constants.ALL_BRANCHES[i]
            checkboxLinearLayout.addView(checkBox)
        }
    }

    private fun submitNewCompanyDetails() {
        /**Here we get the text from editText and trim the space */
        val companyName: String = et_new_company_name.text.toString().trim { it <= ' ' }
        val cgpaCutOffString: String = et_cgpa_cut_off.text.toString().trim { it <= ' ' }
        val ctcDetails = et_ctc_details.text.toString().trim { it <= ' ' }
        val jobProfile = et_job_profile.text.toString().trim { it <= ' ' }
        val companyLocation: String = et_location_new_company.text.toString().trim { it <= ' ' }
        val deadlineToApply: String = et_deadline_to_apply.text.toString().trim { it <= ' ' }
        val backLogsAllowed: Int = if (rb_backlogs_allowed.isSelected) 1 else 0

        val branchesAllowed: ArrayList<String> = ArrayList<String>()

        /** create list of branches which are checked by user */
        for (i in 0 until ll_check_boxes.childCount) {
            val checkBox: CheckBox = ll_check_boxes.getChildAt(i) as CheckBox
            if (checkBox.isChecked) {
                branchesAllowed.add(checkBox.text.toString())
            }
        }

        if (validateForm(
                companyName,
                cgpaCutOffString,
                branchesAllowed,
                ctcDetails,
                jobProfile,
                deadlineToApply
            )
        ) {
            val cgpaCutOff = cgpaCutOffString.toDouble()
            var deadlineLong : Long =0
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                val date = sdf.parse(deadlineToApply)
                deadlineLong = date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            /** convert company name to upper case */
            var name=""
            for(character in companyName){
                name += character.toUpperCase()
            }

            /** create company object */
            val company = Company(
                name, cgpaCutOff,
                backLogsAllowed, branchesAllowed, ctcDetails,
                companyLocation, deadlineLong, jobProfile,
                ArrayList(), 0
            )
            this.company=company

            showProgressDialog(resources.getString(R.string.please_wait))
            /**Get eligible students list according to company constraints */
            FirestoreClass().getEligibleStudents(company, collegeCode, this)
        }
    }

    /** Successfully fetched list of eligible students according to company constraints */
    fun getEligibleStudentsSuccess(eligibleStudents: ArrayList<Student>, companyName: String) {

        this.eligibleStudents = eligibleStudents

        /** Notify all these eligible students regarding new company
        This will be done in background using Async tasks*/
        for (student in eligibleStudents) {
            val token = student.fcmToken
            val id = student.id
            eligibleStudentsIds.add(id)
            val title = "$companyName hiring"
            val message = "$companyName's details have been added."
            SendNotificationToEligibleStudentsAsyncTask(title,message, token).execute()
        }

        /** Update the eligible student's database */
        val companyLastRoundObject = CompanyNameAndLastRound(companyName, 1,1)
        val previousCompanyLastRoundObject = CompanyNameAndLastRound(company.name, -1,2)
        FirestoreClass().updateCompanyStatusInStudentDatabase(
            0,
            eligibleStudentsIds,
            companyLastRoundObject,
            previousCompanyLastRoundObject,
            this,
            selectedStudentsUpdated = false,
            updatePlacedField = false
        )
    }

    fun updateCompanyInStudentDatabaseSuccess(studentIds : ArrayList<String>) {
        /** Update company database **/
        val round = Round()
        round.name = Constants.SCREENING_ROUND
        round.number =1
        round.date =System.currentTimeMillis()
        round.time=SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        round.venue = "online"
        round.isOver=1
        round.selectedStudents=studentIds

        /** is no student is shortlisted in the screening
         * round, then process is over */
        if(studentIds.isEmpty()){
            company.roundsOver=1
        }

        company.roundsList.add(round)
        FirestoreClass().addCompanyInCollege(company, collegeCode, this)
    }

    fun companyRegisteredSuccess() {
        Toast.makeText(
            this, getString(R.string.company_is_added),
            Toast.LENGTH_LONG
        ).show()

        FirestoreClass().getAllStudents(collegeCode,this)
    }

    fun getAllStudentsSuccess(students : ArrayList<Student>){
        hideProgressDialog()

        val notEligibleStudentIds = ArrayList<String>()
        for(student in students){
            val title = "Screening round not cleared"
            val message = "You have not cleared the screening round of ${company.name}"
            /** if student is not in eligible students,
             * then he/she has not cleared the screening round */
            if(!(eligibleStudentsIds.contains(student.id))){
                notEligibleStudentIds.add(student.id)
                SendNotificationToEligibleStudentsAsyncTask(title,message, student.fcmToken).execute()
            }
        }

        /** Update not eligible student's database */
        val companyLastRoundObject = CompanyNameAndLastRound(company.name, 1,0)
        val previousCompanyLastRoundObject = CompanyNameAndLastRound(company.name, -1,2)
        FirestoreClass().updateCompanyStatusInStudentDatabase(
            0,
            notEligibleStudentIds,
            companyLastRoundObject,
            previousCompanyLastRoundObject,
            this,
            selectedStudentsUpdated = true,
            updatePlacedField = false
        )
    }

    fun notEligibleStudentsDatabaseSuccess(){
        hideProgressDialog()
        finish()
    }

    /** A function to validate the details of a new company.*/
    private fun validateForm(
        companyName: String,
        cgpaCutOff: String,
        branchesAllowed: ArrayList<String>,
        ctcDetails: String,
        jobProfile: String,
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
            cgpaCutOff.toFloat() < 0 -> {
                showErrorSnackBar(getString(R.string.valid_cgpa))
                false
            }
            cgpaCutOff.toFloat() > 10 -> {
                showErrorSnackBar(getString(R.string.valid_cgpa))
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
            TextUtils.isEmpty(jobProfile) -> {
                showErrorSnackBar(getString(R.string.enter_job_profile))
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
