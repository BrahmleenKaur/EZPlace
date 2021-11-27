package com.example.ezplace.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirebaseAuthClass
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_add_pr.*

class AddPrActivity : BaseActivity() {

    private var collegeCode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pr)
        setupActionBar(toolbar_add_pr_activity)

        /** Initialise college code passed from intent */
        if (intent.hasExtra(Constants.COLLEGE_CODE))
            collegeCode = intent.getStringExtra(Constants.COLLEGE_CODE)!!

        /**Click listener for button */
        btn_add_pr.setOnClickListener {
            addPR()
        }
    }

    /** Function to add a PR */
    private fun addPR() {
        /** Here we get the text from editText and trim the space */
        val firstName: String = et_first_name_add_pr.text.toString().trim { it <= ' ' }
        val lastName: String = et_last_name_add_pr.text.toString().trim { it <= ' ' }
        val email: String = et_email_add_pr.text.toString().trim { it <= ' ' }
        val password: String = et_password_add_pr.text.toString().trim { it <= ' ' }
        val confirmPassword: String = et_confirm_password_add_pr.text.toString().trim { it <= ' ' }

        /** Validate the user input first, the function is defined in base activity **/
        if (validateForm(
                firstName = firstName,
                email = email,
                password = password,
                confirmPassword = confirmPassword
            )
        ) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))
            /** PR object */
            val PR = TPO()
            PR.firstName = firstName
            PR.lastName = lastName
            PR.email = email
            PR.collegeCode = collegeCode
            /** Sign-In using FirebaseAuth */
            FirebaseAuthClass().signUpTPO(PR, password, this)
        }
    }

    /**
     * A function to validate the entries of a user.
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
            (TextUtils.isEmpty(email)) -> {
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
            else -> true
        }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "You will have to sign in again.", Toast.LENGTH_LONG).show()

        /** Firebase automatically signs in the user who signed up
         * so here TPO/PR will have to sign in again with their account
         */
        FirebaseAuthClass().signOut()
        val mSharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)
        if (mSharedPreferences.contains(Constants.PR_EMAIL)) {
            mSharedPreferences.edit().remove(Constants.PR_EMAIL).apply()
            mSharedPreferences.edit().remove(Constants.PR_PASSWORD).apply()
        }
        if (mSharedPreferences.contains(Constants.STUDENT_EMAIL)) {
            mSharedPreferences.edit().remove(Constants.FCM_TOKEN_UPDATED).apply()
            mSharedPreferences.edit().remove(Constants.STUDENT_EMAIL).apply()
            mSharedPreferences.edit().remove(Constants.STUDENT_PASSWORD).apply()
        }
        intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

    fun addPrSuccess() {
        hideProgressDialog()
        Toast.makeText(this, "New PR added successfully", Toast.LENGTH_LONG).show()
    }
}