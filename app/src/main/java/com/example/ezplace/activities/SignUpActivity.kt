package com.example.ezplace.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirebaseAuthClass
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.College
import com.example.ezplace.models.Student
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {

    private var isStudent: Boolean = true
    private lateinit var mSharedPreferences: SharedPreferences
    var email =""
    var password =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        /** Initialize "isStudent" */
        var extras: Bundle? = intent.extras
        isStudent = extras!!.getBoolean(Constants.IS_STUDENT)

        mSharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)

        /** disables collegeName text view for student in layout */
        if (isStudent) til_college_name_sign_up.visibility = View.GONE
        else til_college_name_sign_up.visibility = View.VISIBLE

        fullScreenMode()
        setupActionBar(toolbar_sign_up)

        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    /** A function to register a user to the app using the Firebase. */
    private fun registerUser() {
        /** Here we get the text from editText and trim the space */
        val firstName: String = et_first_name_sign_up.text.toString().trim { it <= ' ' }
        val lastName: String = et_last_name_sign_up.text.toString().trim { it <= ' ' }
        email= et_email_sign_up.text.toString().trim { it <= ' ' }
        password= et_password_sign_up.text.toString().trim { it <= ' ' }
        val confirmPassword: String = et_confirm_password_sign_up.text.toString().trim { it <= ' ' }
        val collegeName: String = et_college_name_sign_up.text.toString().trim { it <= ' ' }

        if (validateForm(firstName, email, password, confirmPassword)) {

            if (isStudent) {
                val student = Student()
                student.firstName = firstName
                student.lastName = lastName
                student.email = email

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))
                FirebaseAuthClass().signUpStudent(student, password, this)
            } else {
                val tpo = TPO()
                tpo.firstName = firstName
                tpo.lastName = lastName
                tpo.collegeName = collegeName
                tpo.email = email

                val college = College()
                college.collegeName = collegeName

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().registerCollege(college, tpo, password, this)
            }


        }

    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateForm(
        firstName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(firstName) -> {
                showErrorSnackBar(getString(R.string.enter_first_name))
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(getString(R.string.enter_email))
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showErrorSnackBar(getString(R.string.wrong_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(getString(R.string.enter_password))
                false
            }
            password != confirmPassword -> {
                showErrorSnackBar(getString(R.string.passwords_not_matching))
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to be called after the student is registered successfully
     * and entry is made in the firestore database.
     */
    fun studentRegisteredSuccess() {
        // Hide the progress dialog
        hideProgressDialog()

        /**Add to sharedPreference */
        mSharedPreferences.edit().putString(Constants.STUDENT_EMAIL,email).apply()
        mSharedPreferences.edit().putString(Constants.STUDENT_PASSWORD,password).apply()

        //adding toast
        Toast.makeText(
            this,
            "${FirebaseAuthClass().getCurrentUserMailId()} has successfully registered with EZ Place!",
            Toast.LENGTH_LONG
        ).show()

        /** Sends the student to Update Profile activity after sign-up */
        val intent = Intent(this, UpdateProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Finish the Sign-Up Screen
        finish()
    }

    /**
     * A function to be called after the TPO is registered successfully
     * and entry is made in the firestore database.
     */
    fun tpoRegisteredSuccess(tpo: TPO) {
        // Hide the progress dialog
        hideProgressDialog()

        //adding toast
        Toast.makeText(
            this,
            "${FirebaseAuthClass().getCurrentUserMailId()} has successfully registered with EZ Place!",
            Toast.LENGTH_LONG
        ).show()

        /** TPO is sent to Main activity after sign-up */
        intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.TPO_DETAILS, tpo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Finish the Sign-Up Screen
        this.finish()
    }
}