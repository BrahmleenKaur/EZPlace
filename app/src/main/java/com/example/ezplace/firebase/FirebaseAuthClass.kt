package com.example.ezplace.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.ezplace.activities.SignInActivity
import com.example.ezplace.activities.SignUpActivity
import com.example.ezplace.models.Student
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class FirebaseAuthClass() {
    private lateinit var auth: FirebaseAuth

    fun signUp(student : Student, password: String, activity : SignUpActivity){
        val email = student.email
        auth = Firebase.auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->

                // If the registration is successfully done
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    student.id = firebaseUser.uid
                    // call the registerUser function of FirestoreClass to make an entry in the database.
                    FirestoreClass().registerStudent(activity, student)

                } else {
                    Toast.makeText(
                        activity,
                        task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("signuperror", "${task.exception!!.message}")
                    activity.hideProgressDialog()
                }

            }
    }

    fun signIn(email: String, password: String, activity : SignInActivity) {
        auth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirestoreClass().loadStudentData(activity)
                } else {
                    Toast.makeText(
                        activity,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("signinerror", "${task.exception!!.message}")
                    activity.hideProgressDialog()
                }
            }
    }

    fun signOut(){
        auth = Firebase.auth
        auth.signOut()
    }

    fun getCurrentUserID(): String {
       auth = Firebase.auth
        val user = auth.currentUser
        if(user != null){
            return user.uid
        }
        return ""
    }

    fun getCurrentUserMailId(): String? {
        auth = Firebase.auth
        var user = auth.currentUser
        if(user != null) return user.email
        return ""
    }
}