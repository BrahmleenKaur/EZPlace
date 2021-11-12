package com.example.ezplace.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_update_profile.*

class UpdateProfileActivity : BaseActivity() {

    // A global variable for user details.
    private lateinit var mStudentDetails: Student

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)
        setupActionBar(toolbar_update_profile)

        FirestoreClass().loadStudentData(this)

        btn_update.setOnClickListener {

            showProgressDialog(resources.getString(R.string.please_wait))
            // Call a function to update user details in the database.
            updateStudentProfileData()
        }
    }

    /**
     * A function to update the user profile details into the database.
     */
    private fun updateStudentProfileData() {

        val userHashMap = HashMap<String, Any>()


        if (et_first_name_update_profile.text.toString() != mStudentDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = et_first_name_update_profile.text.toString()
        }
        if (et_last_name_update_profile.text.toString() != mStudentDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = et_last_name_update_profile.text.toString()
        }
        if (et_college_code_update_profile.text.toString() != mStudentDetails.collegeCode) {
            userHashMap[Constants.COLLEGE_CODE] = et_college_code_update_profile.text.toString()
        }
        if (et_branch_update_profile.text.toString() != mStudentDetails.branchCode) {
            userHashMap[Constants.BRANCH_CODE] = et_branch_update_profile.text.toString()
        }
        if (et_cgpa_update_profile.text.toString() != mStudentDetails.cgpa.toString()) {
            userHashMap[Constants.CGPA] = et_cgpa_update_profile.text.toString().toDouble()
        }
        // Update the data in the database.
        FirestoreClass().updateUserProfileData(this@UpdateProfileActivity, userHashMap)
    }

    /**
     * A function to set the existing details in UI.
     */
    fun setStudentDataInUI(student : Student) {

        // Initialize the user details variable
        mStudentDetails = student

        et_first_name_update_profile.setText(student.firstName)
        et_last_name_update_profile.setText(student.lastName)
        et_email_update_profile.setText(student.email)
    }

    /**
     * A function to notify the user profile is updated successfully.
     */
    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_LONG).show()
        intent = Intent(this, MainActivity::class.java)
//        intent.putExtra(Constants.STUDENT_DETAILS, student)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

}
