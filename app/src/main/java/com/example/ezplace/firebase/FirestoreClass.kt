package com.example.ezplace.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.ezplace.activities.SignInActivity
import com.example.ezplace.activities.SignUpActivity
import com.example.ezplace.activities.UpdateProfileActivity
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerStudent(activity : SignUpActivity, studentInfo: Student) {
        mFireStore.collection(Constants.STUDENTS)
            // Here the document id is the Student ID.
            .document(studentInfo.id)
            // Here the studentInfo are field values and the SetOption is set to merge. It is for if we want to merge
            .set(studentInfo, SetOptions.merge())
            .addOnSuccessListener {
                // Here call a function of base activity for transferring the result to it.
                activity.studentRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document",
                    e
                )
            }
    }

    fun loadStudentData(activity: Activity) {
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.STUDENTS)
            // The document id to get the Fields of user.
            .document(FirebaseAuthClass().getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.i("detail", document.toString())

                // Here we have received the document snapshot which is converted into the Student Data model object.
                val loggedInUser = document.toObject(Student::class.java)!!

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is UpdateProfileActivity -> {
                        activity.setStudentDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
//                  activity.hideProgressDialog()
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is UpdateProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user details",
                    e
                )
            }
    }

    fun updateUserProfileData(activity: UpdateProfileActivity, userHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.STUDENTS) // Collection Name
            .document(FirebaseAuthClass().getCurrentUserID()) // Document ID
            .update(userHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                // Notify the success result.
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating profile.",
                    e
                )
            }
    }


}