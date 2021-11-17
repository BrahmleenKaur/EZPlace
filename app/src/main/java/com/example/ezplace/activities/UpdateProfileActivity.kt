package com.example.ezplace.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_new_company_details.*
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfileActivity : BaseActivity() {

    /**A global variable for student details. */
    private lateinit var mStudentDetails: Student

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        setupActionBar(toolbar_update_profile)

        addBranchesRadioButtonsInLayout()

        /** Load student data from database */
        showProgressDialog(getString((R.string.please_wait)))
        FirestoreClass().loadStudentData(this)

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

    /**
     * A function to set the existing details in UI.
     */
    fun setStudentDataInUI(student: Student) {
        hideProgressDialog()
        // Initialize the user details variable
        mStudentDetails = student

        et_first_name_update_profile.setText(student.firstName)
        et_last_name_update_profile.setText(student.lastName)
        et_roll_number_update_profile.setText(student.rollNumber)
        et_college_code_update_profile.setText(student.collegeCode)

        for (i in 0 until rg_branches_update_profile.childCount) {

            val radioButton = rg_branches_update_profile.getChildAt(i) as RadioButton
            if (radioButton.text.toString() == student.branch) {
                rg_branches_update_profile.check(radioButton.id)
                break
            }
        }

        et_cgpa_update_profile.setText(student.cgpa.toString())
        et_backlogs.setText(student.numberOfBacklogs.toString())
    }

    /**
     * A function to update the user profile details into the database.
     */
    private fun updateStudentProfileData() {

        /** Read the data from layout and trim the space */
        val firstname = et_first_name_update_profile.text.toString().trim { it <= ' ' }
        val lastName = et_last_name_update_profile.text.toString().trim { it <= ' ' }
        val rollNumber = et_roll_number_update_profile.text.toString().trim { it <= ' ' }
        val collegeCode = et_college_code_update_profile.text.toString().trim { it <= ' ' }
        val selectedBranchId = rg_branches_update_profile.checkedRadioButtonId
        val cgpa = et_cgpa_update_profile.text.toString().trim { it <= ' ' }
        val backlogs = et_backlogs.text.toString().trim { it <= ' ' }

        /** Validate inputs */
        if (validateStudentDetails(
                firstname,
                rollNumber,
                collegeCode,
                selectedBranchId,
                cgpa,
                backlogs
            )
        ) {

            /** Create a hashmap of fields to be updated */
            val userHashMap = HashMap<String, Any>()

            /** Check if the entered value is same as previous vale */
            if (firstname != mStudentDetails.firstName) {
                userHashMap[Constants.FIRST_NAME] = firstname
            }
            if (lastName != mStudentDetails.lastName) {
                userHashMap[Constants.LAST_NAME] = lastName
            }
            if (rollNumber != mStudentDetails.rollNumber) {
                userHashMap[Constants.ROLL_NUMBER] = rollNumber
            }
            if (collegeCode != mStudentDetails.collegeCode) {
                userHashMap[Constants.COLLEGE_CODE] = collegeCode
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
                mStudentDetails.firstName = firstname
                mStudentDetails.lastName = lastName
                mStudentDetails.rollNumber = rollNumber
                mStudentDetails.branch = selectedBranch
                mStudentDetails.collegeCode = collegeCode
                mStudentDetails.cgpa = cgpa.toDouble()
                mStudentDetails.numberOfBacklogs = backlogs.toInt()

                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().updateStudentProfileData(this@UpdateProfileActivity, userHashMap)
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
            TextUtils.isEmpty(collegeCode) -> {
                showErrorSnackBar(getString(R.string.enter_your_college_code))
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
            TextUtils.isEmpty(backlogs) -> {
                showErrorSnackBar(getString(R.string.enter_backlogs))
                false
            }
            else -> {
                true
            }
        }
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

}
