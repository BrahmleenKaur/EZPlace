package com.example.ezplace.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirebaseAuthClass
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_add_pr.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class AddPrActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pr)

        setupActionBar(toolbar_add_pr_activity)
        btn_add_pr.setOnClickListener {
            addPR()
        }

    }

    private fun addPR() {
        /** Here we get the text from editText and trim the space */
        val firstName: String = et_first_name_add_pr.text.toString().trim { it <= ' ' }
        val lastName: String = et_last_name_add_pr.text.toString().trim { it <= ' ' }
        val email: String = et_email_add_pr.text.toString().trim { it <= ' ' }
        val password: String = et_password_add_pr.text.toString().trim { it <= ' ' }
        val confirmPassword: String = et_confirm_password_add_pr.text.toString().trim { it <= ' ' }

        if (validateForm(firstName,email, password,confirmPassword)) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            var collegeCode = ""
            if (intent.hasExtra(Constants.COLLEGE_CODE))
                collegeCode = intent.getStringExtra(Constants.COLLEGE_CODE)!!

            var PR= TPO()
            PR.firstName = firstName
            PR.lastName = lastName
            PR.email = email
            PR.collegeCode = collegeCode
            PR.collegeName = collegeCode
            // Sign-In using FirebaseAuth
            FirebaseAuthClass().signUpTPO(PR,password,this)
        }
    }

    /**
     * A function to validate the entries of a user.
     */
    private fun validateForm(firstName : String,email: String, password: String,confirmPassword : String): Boolean {

        return when{
            TextUtils.isEmpty(firstName) -> {
                showErrorSnackBar(getString(R.string.enter_first_name))
                false
            }
            (TextUtils.isEmpty(email)) ->{
                showErrorSnackBar(getString(R.string.enter_email))
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->{
                showErrorSnackBar(getString(R.string.wrong_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(getString(R.string.enter_password))
                false
            }
            password != confirmPassword ->{
                showErrorSnackBar(getString(R.string.passwords_not_matching))
                false
            }
            else -> true
        }
    }

    fun addPrSuccess(){
        hideProgressDialog()

        Toast.makeText(this,"New PR added successfully", Toast.LENGTH_LONG).show()
    }
}