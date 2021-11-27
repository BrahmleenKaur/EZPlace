package com.example.ezplace.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.CompanyNameAndLastRound
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_new_company_details.*
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfileActivity : BaseActivity() {

    /**A global variable for student details. */
    private lateinit var mStudentDetails: Student

    /** Create a hashmap of fields to be updated */
    private val userHashMap = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        setupActionBar(toolbar_update_profile)

        addBranchesRadioButtonsInLayout()
        addCollegeNamesInLayout()

        btn_update.setOnClickListener {
            /** Call a function to update user details in the database. */
            updateStudentProfileData()
        }
    }

    /** Show branches list in layout */
    private fun addBranchesRadioButtonsInLayout() {
        val branchesRadioGroup = rg_branches_update_profile

        for (i in Constants.ALL_BRANCHES.indices) {
            val radioButton = RadioButton(this)
            radioButton.text = Constants.ALL_BRANCHES[i]
            branchesRadioGroup.addView(radioButton)
        }
    }

    /** Show college names in layout */
    private fun addCollegeNamesInLayout() {
        /** First fetch the college names from the database */
        showProgressDialog(getString((R.string.please_wait)))
        FirestoreClass().getCollegeNames(this)
    }

    /** College Names successfully retrieved , now add them in layout */
    fun getCollegeNamesSuccess(collegeNames: ArrayList<String>) {

        collegeNames.add(0, Constants.SELECT_COLLEGE_NAME)
        /**create an adapter to describe how the items are displayed*/
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, collegeNames)
        /**set the spinners adapter to the previously created one.*/
        dropdown_college_name.adapter = adapter
        dropdown_college_name.setSelection(0)

        /** Now load student data from database */
        FirestoreClass().loadStudentData(this)
    }

    /**
     * A function to set the existing details in UI.
     */
    fun setStudentDataInUI(student: Student) {
        // Initialize the user details variable
        mStudentDetails = student

        et_first_name_update_profile.setText(student.firstName)
        et_last_name_update_profile.setText(student.lastName)
        et_roll_number_update_profile.setText(student.rollNumber)

        val dropdownAdapter = dropdown_college_name.adapter as ArrayAdapter<String>
        val collegeNamePosition = dropdownAdapter.getPosition(student.collegeCode)
        dropdown_college_name.setSelection(collegeNamePosition)

        /** Select that branch's radio button which matches student's details*/
        for (i in 0 until rg_branches_update_profile.childCount) {
            val radioButton = rg_branches_update_profile.getChildAt(i) as RadioButton
            if (radioButton.text.toString() == student.branch) {
                rg_branches_update_profile.check(radioButton.id)
                break
            }
        }

        et_cgpa_update_profile.setText(student.cgpa.toString())
        et_backlogs.setText(student.numberOfBacklogs.toString())

        /** Get college details to check if update
         * profile button is to be enabled or disabled */
        if (mStudentDetails.collegeCode.isNotEmpty())
            getCollegeDetails()
        else
            hideProgressDialog()

    }

    /** Fetch college details from database */
    private fun getCollegeDetails() {
        FirestoreClass().getCollege(mStudentDetails.collegeCode, this)
    }

    /** College details are fetched successfully */
    fun getCollegeSuccess(isUpdateButtonEnabled: Long) {
        hideProgressDialog()
        val one: Long = 1
        if (isUpdateButtonEnabled != one) {
            btn_update.background =
                ContextCompat.getDrawable(this, R.drawable.grey_border_shape_button_rounded)

            /**Tell the student that button is disabled by TPO*/
            btn_update.setOnClickListener {
                Toast.makeText(this, "Update button is disabled by the TPO.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    /**
     * A function to update the user profile details into the database.
     */
    private fun updateStudentProfileData() {
        /** Read the data from layout and trim the space */
        val firstname = et_first_name_update_profile.text.toString().trim { it <= ' ' }
        val lastName = et_last_name_update_profile.text.toString().trim { it <= ' ' }
        val rollNumber = et_roll_number_update_profile.text.toString().trim { it <= ' ' }
        val collegeName = dropdown_college_name.selectedItem.toString()
        val selectedBranchId = rg_branches_update_profile.checkedRadioButtonId
        val cgpa = et_cgpa_update_profile.text.toString().trim { it <= ' ' }
        val backlogs = et_backlogs.text.toString().trim { it <= ' ' }

        /** Validate inputs */
        if (validateStudentDetails(
                firstname,
                rollNumber,
                collegeName,
                selectedBranchId,
                cgpa,
                backlogs
            )
        ) {
            /** Check if the entered value is same as previous value */
            if (firstname != mStudentDetails.firstName) {
                userHashMap[Constants.FIRST_NAME] = firstname
            }
            if (lastName != mStudentDetails.lastName) {
                userHashMap[Constants.LAST_NAME] = lastName
            }
            if (rollNumber != mStudentDetails.rollNumber) {
                userHashMap[Constants.ROLL_NUMBER] = rollNumber
            }
            if (collegeName != mStudentDetails.collegeCode) {
                userHashMap[Constants.COLLEGE_CODE] = collegeName
            }

            val selectedBranchRadioButton: RadioButton = findViewById(selectedBranchId)
            val selectedBranch: String = selectedBranchRadioButton.text.toString()
            if (selectedBranch != mStudentDetails.branch) {
                userHashMap[Constants.BRANCH] = selectedBranch
            }
            if (cgpa != mStudentDetails.cgpa.toString()) {
                userHashMap[Constants.CGPA] = cgpa.toDouble()
            }
            if (backlogs != mStudentDetails.numberOfBacklogs.toString()) {
                userHashMap[Constants.NUMBER_OF_BACKLOGS] = backlogs.toInt()
            }

            /** Update the data in the database. */
            if (userHashMap.size > 0) {
                val isRollNumberEmpty = mStudentDetails.rollNumber.isEmpty()

                /** Set the mStudents global variable */
                mStudentDetails.firstName = firstname
                mStudentDetails.lastName = lastName
                mStudentDetails.rollNumber = rollNumber
                mStudentDetails.branch = selectedBranch
                mStudentDetails.collegeCode = collegeName
                mStudentDetails.cgpa = cgpa.toDouble()
                mStudentDetails.numberOfBacklogs = backlogs.toInt()

                /** If the roll number is empty, then that means the student
                 * is updating profile for the first time after sign up,
                 * we need to update this students database with the companies
                 * which are already stored in the database  */
                if (isRollNumberEmpty) {
                    showProgressDialog(getString(R.string.please_wait))
                    FirestoreClass().getEligibleCompaniesNamesForOneStudent(mStudentDetails, this)
                } else {
                    showProgressDialog(resources.getString(R.string.please_wait))
                    FirestoreClass().updateStudentProfileData(
                        this@UpdateProfileActivity,
                        userHashMap
                    )
                }
            } else {
                /** if no changes detected, then just send to Main activity */
                intent = Intent(this, MainActivity::class.java)
                intent.putExtra(Constants.STUDENT_DETAILS, mStudentDetails)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                this.finish()
            }
        }
    }

    /** companies for a student are loaded successfully
     * This is called when student has signed up for the first time
     * so might have missed companies which are already in the database */
    fun getEligibleCompaniesNamesSuccess(companies: ArrayList<Company>) {
        val companyHashMap = HashMap<String, Any>()

        /** add the current student's names to the companies' selected students */
        for (company in companies) {
            company.roundsList[0].selectedStudents.add(mStudentDetails.id)
            companyHashMap[Constants.ROUNDS_LIST] = company.roundsList
            FirestoreClass().updateCompanyInCollegeDatabase(
                companyHashMap,
                company.name,
                mStudentDetails.collegeCode,
                this
            )
        }

        /** Update student profile */
        val companyNamesAndLastRounds = ArrayList<CompanyNameAndLastRound>()
        for (company in companies) {
            val companyNameAndLastRoundObject = CompanyNameAndLastRound()
            companyNameAndLastRoundObject.companyName = company.name
            companyNameAndLastRoundObject.lastRound = 1
            companyNameAndLastRoundObject.lastRoundCleared = 1
            companyNamesAndLastRounds.add(companyNameAndLastRoundObject)
        }
        userHashMap[Constants.COMPANY_NAME_AND_LAST_ROUND] = companyNamesAndLastRounds
        mStudentDetails.companiesListAndLastRound = companyNamesAndLastRounds

        FirestoreClass().updateStudentProfileData(this@UpdateProfileActivity, userHashMap)
    }

    /**
     * A function to notify the user profile is updated successfully.
     */
    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_LONG).show()

        /** Send the student to Main activity */
        intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.STUDENT_DETAILS, mStudentDetails)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

    /** Function to validate input given by user */
    private fun validateStudentDetails(
        firstName: String, rollNumber: String,
        collegeCode: String, branchId: Int, cgpa: String, backlogs: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(firstName) -> {
                showErrorSnackBar(getString(R.string.enter_first_name))
                false
            }
            TextUtils.isEmpty(rollNumber) -> {
                showErrorSnackBar(getString(R.string.enter_roll_number))
                false
            }
            collegeCode == Constants.SELECT_COLLEGE_NAME -> {
                showErrorSnackBar(getString(R.string.select_college_name))
                false
            }
            branchId == -1 -> {
                showErrorSnackBar(getString(R.string.select_you_branch))
                false
            }
            TextUtils.isEmpty(cgpa) -> {
                showErrorSnackBar(getString(R.string.enter_cgpa))
                false
            }
            cgpa.toFloat() < 1 -> {
                showErrorSnackBar(getString(R.string.valid_cgpa))
                false
            }
            cgpa.toFloat() > 10 -> {
                showErrorSnackBar(getString(R.string.valid_cgpa))
                false
            }
            TextUtils.isEmpty(backlogs) -> {
                showErrorSnackBar(getString(R.string.enter_backlogs))
                false
            }
            else -> {
                true
            }
        }
    }

}
