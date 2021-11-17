package com.example.ezplace.activities

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        var extras : Bundle? = intent.extras

        isStudent = extras!!.getBoolean(Constants.IS_STUDENT)

        if(isStudent) til_college_name_sign_up.visibility= View.GONE
        else til_college_name_sign_up.visibility= View.VISIBLE

        fullScreenMode()
        setupActionBar(toolbar_sign_up)

        btn_sign_up.setOnClickListener {
            registerUser()
        }
    }

    /**
     * A function to register a user to our app using the Firebase.
     * For more details visit: https://firebase.google.com/docs/auth/android/custom-auth
     */
    private fun registerUser() {
        // Here we get the text from editText and trim the space
        val firstName: String = et_first_name_sign_up.text.toString().trim { it <= ' ' }
        val lastName: String = et_last_name_sign_up.text.toString().trim { it <= ' ' }
        val email: String = et_email_sign_up.text.toString().trim { it <= ' ' }
        val password: String = et_password_sign_up.text.toString().trim { it <= ' ' }
        val collegeName: String = et_college_name_sign_up.text.toString().trim { it <= ' ' }

        if (validateForm(firstName,email, password)) {

            if(isStudent){
                val student = Student(companiesListAndLastRound = ArrayList())
                student.firstName=firstName
                student.lastName=lastName
                student.email=email

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))
                FirebaseAuthClass().signUpStudent(student,password,this)
            }
            else{
                val tpo = TPO()
                tpo.firstName=firstName
                tpo.lastName=lastName
                tpo.collegeName = collegeName
                tpo.email=email

                val college = College()

                // Show the progress dialog.
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().registerCollege(college,tpo,password,this)
            }


        }

    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateForm(firstName: String,email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(firstName) -> {
                showErrorSnackBar(getString(R.string.enter_first_name))
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(getString(R.string.enter_email))
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(getString(R.string.enter_password))
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to be called the user is registered successfully and entry is made in the firestore database.
     */
    fun studentRegisteredSuccess() {
        // Hide the progress dialog
        hideProgressDialog()
        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        //adding toast
        Toast.makeText(this,
            "${FirebaseAuthClass().getCurrentUserMailId()} has successfully registered with EZ Place!",
            Toast.LENGTH_LONG).show()

        val intent = Intent(this,UpdateProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Finish the Sign-Up Screen
        finish()
    }

    fun tpoRegisteredSuccess(tpo : TPO){
        // Hide the progress dialog
        hideProgressDialog()
        /**
         * Here the new user registered is automatically signed-in
         */
        //adding toast
        Toast.makeText(this,
            "${FirebaseAuthClass().getCurrentUserMailId()} has successfully registered with EZ Place!",
            Toast.LENGTH_LONG).show()
        intent = Intent(this, MainActivity::class.java)
        intent.putExtra(Constants.TPO_DETAILS, tpo)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Finish the Sign-Up Screen
        this.finish()
    }
}