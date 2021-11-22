package com.example.ezplace.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirebaseAuthClass
import com.example.ezplace.models.Student
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : BaseActivity() {

    private lateinit var mSharedPreferences: SharedPreferences
    private var email =""
    private var password =""
    private var isPr = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        fullScreenMode()

        mSharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)

        setupActionBar(toolbar_sign_in_activity)

        if(intent.hasExtra(Constants.IS_PR)){
            isPr = true
        }
        else if(intent.hasExtra(Constants.PR_EMAIL)){
            isPr = true
            val extras : Bundle = intent.extras!!
            email = extras.getString(Constants.PR_EMAIL,"default")
            password = extras.getString(Constants.PR_PASSWORD,"default")
            // Sign-In using FirebaseAuth
            showProgressDialog(getString(R.string.please_wait))
            FirebaseAuthClass().signIn(email, password,this)
        }
        else if(intent.hasExtra(Constants.STUDENT_EMAIL)){
            val extras : Bundle = intent.extras!!
            email = extras.getString(Constants.STUDENT_EMAIL,"default")
            password = extras.getString(Constants.STUDENT_PASSWORD,"default")
            // Sign-In using FirebaseAuth
            showProgressDialog(getString(R.string.please_wait))
            FirebaseAuthClass().signIn(email, password,this)
        }

        btn_sign_in.setOnClickListener{
            signInRegisteredUser()
        }
    }

    /**
     * A function for Sign-In using the registered user using the email and password.
     */
    private fun signInRegisteredUser() {
        // Here we get the text from editText and trim the space
        email = et_email_sign_in.text.toString().trim { it <= ' ' }
        password = et_password_sign_in.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Sign-In using FirebaseAuth
            FirebaseAuthClass().signIn(email, password,this)
        }
    }

    /**
     * A function to validate the entries of a user.
     */
    private fun validateForm(email: String, password: String): Boolean {

        return when{
            (TextUtils.isEmpty(email)) ->{
                showErrorSnackBar(getString(R.string.enter_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(getString(R.string.enter_password))
                false
            }
            else -> true
        }
    }

    /**
     * After successful sign-in , sends the student to Main activity
     */
    fun signInSuccessByStudent(student: Student) {
        hideProgressDialog()

        /**Add to sharedPreference */
        mSharedPreferences.edit().putString(Constants.STUDENT_EMAIL,email).apply()
        mSharedPreferences.edit().putString(Constants.STUDENT_PASSWORD,password).apply()

        Toast.makeText(this, "${student.firstName} signed in successfully.", Toast.LENGTH_LONG).show()
        intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.STUDENT_DETAILS, student)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }

    /**
     * After successful sign-in , sends the TPO to Main activity
     */
    fun signInSuccessByTPO(tpo: TPO) {
        hideProgressDialog()

        /**Add to sharedPreference */
        if(isPr){
            mSharedPreferences.edit().putString(Constants.PR_EMAIL,email).apply()
            mSharedPreferences.edit().putString(Constants.PR_PASSWORD,password).apply()
        }

        Toast.makeText(this, "${tpo.firstName} signed in successfully.", Toast.LENGTH_LONG).show()
        intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.TPO_DETAILS, tpo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        this.finish()
    }
}