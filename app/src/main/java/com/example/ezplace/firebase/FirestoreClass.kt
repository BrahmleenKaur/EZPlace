package com.example.ezplace.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.ezplace.activities.*
import com.example.ezplace.models.*
import com.example.ezplace.utils.Constants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFireStore = FirebaseFirestore.getInstance()

    /** Register student in firestore after sign-up */
    fun registerStudent(activity: SignUpActivity, studentInfo: Student) {
        val id = studentInfo.id
        mFireStore.collection(Constants.STUDENTS)
            /** Here the document id is the Student ID. */
            .document(id)
            /** Here the studentInfo are field values and the
             * SetOption is set to merge. It is for if we want to merge */
            .set(studentInfo, SetOptions.merge())
            .addOnSuccessListener {
                /** Here call a function of base activity for transferring the result to it. */
                activity.studentRegisteredSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error registering student",
                    e
                )
            }
    }

    fun registerCollege(college: College, tpo: TPO, password: String, activity: SignUpActivity) {
        mFireStore.collection((Constants.COLLEGES))
            .document(college.collegeName)
            .set(college)
            .addOnSuccessListener {
                tpo.collegeCode = college.collegeName
                FirebaseAuthClass().signUpTPO(tpo, password, activity)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error registering college",
                    e
                )
            }
    }

    fun registerTPO(activity: Activity, tpoInfo: TPO) {
        mFireStore.collection(Constants.TPO)
            // Here the document id is the TPO ID.
            .document(tpoInfo.id)
            // Here the tpoInfo is field values and the SetOption is set to merge. It is for if we want to merge
            .set(tpoInfo, SetOptions.merge())
            .addOnSuccessListener {
                when(activity){
                    is SignUpActivity ->{
                        // Here call a function of base activity for transferring the result to it.
                        activity.tpoRegisteredSuccess(tpoInfo)
                    }
                    is AddPrActivity ->{
                        activity.addPrSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when(activity){
                    is SignUpActivity ->{
                        // Here call a function of base activity for transferring the result to it.
                        activity.hideProgressDialog()
                    }
                    is AddPrActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error registering tpo",
                    e
                )
            }
    }

    fun updateCollege(collegeCode : String,collegeHashmap : HashMap<String,Int>,activity: MainActivity){
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .set(collegeHashmap, SetOptions.merge())
            .addOnSuccessListener {
                activity.updateCollegeSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error updating college",
                    e
                )
            }
    }

    fun getCollege(collegeCode : String, activity: UpdateProfileActivity){
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .get()
            .addOnSuccessListener { document ->
                 var isUpdateButtonEnabled : Long =document[Constants.IS_UPDATE_BUTTON_ENABLED] as Long
                activity.getCollegeSuccess(isUpdateButtonEnabled)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error getting college",
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
                // Here we have received the document snapshot which is converted into the Student Data model object.
                val loggedInUser = document.toObject(Student::class.java)!!

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SplashActivity -> {
                        activity.signInSuccessByStudent(loggedInUser)
                    }
                    is SignInActivity -> {
                        activity.signInSuccessByStudent(loggedInUser)
                    }
                    is UpdateProfileActivity -> {
                        activity.setStudentDataInUI(loggedInUser)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                    is UpdateProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
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

    fun loadStudentOrTPOData(activity: Activity) {
        mFireStore.collection(Constants.TPO)
            // The document id to get the Fields of user.
            .document(FirebaseAuthClass().getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loggedInTPO: TPO = document.toObject(TPO::class.java)!!
                    when (activity) {
                        is SplashActivity -> {
                            activity.signInSuccessByTPO(loggedInTPO)
                        }
                        is SignInActivity -> {
                            activity.signInSuccessByTPO(loggedInTPO)
                        }
                    }
                } else {
                    Log.i("tag", "student")
                    loadStudentData(activity)
                }
            }
            .addOnFailureListener { e ->
                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting loggedIn user or admin details",
                    e
                )
            }
    }

    fun updateStudentProfileData(activity: Activity, studentHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.STUDENTS) // Collection Name
            .document(FirebaseAuthClass().getCurrentUserID()) // Document ID
            .update(studentHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                // Notify the success result.

                when (activity) {
                    is UpdateProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is UpdateProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating profile.",
                    e
                )
            }
    }

    fun addCompanyInCollege(
        company: Company,
        collegeCode: String,
        activity: NewCompanyDetailsActivity
    ) {
        var companyHashMap = HashMap<String, Company>()
        companyHashMap[company.name] = company

        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .collection(Constants.COMPANIES)
            .document(company.name)
            .set(company)
            .addOnSuccessListener {
                activity.companyRegisteredSuccess(company)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding company in college.",
                    e
                )
            }
    }

    fun getEligibleStudents(
        company: Company,
        collegeCode: String,
        activity: NewCompanyDetailsActivity
    ) {
        Log.i("stu3", collegeCode)
        if (company.backLogsAllowed == 0) {
            mFireStore.collection(Constants.STUDENTS)
                .whereEqualTo(Constants.COLLEGE_CODE, collegeCode)
                .whereIn(
                    Constants.BRANCH,
                    company.branchesAllowed
                )
                .whereGreaterThanOrEqualTo(Constants.CGPA, company.cgpaCutOff)
                .whereEqualTo(Constants.NUMBER_OF_BACKLOGS, company.backLogsAllowed)
                .whereEqualTo(Constants.PLACED_ABOVE_THRESHOLD, 0)
                .get()
                .addOnSuccessListener { studentDocuments ->
                    var eligibleStudents: ArrayList<Student> = ArrayList()
                    for (student in studentDocuments) {
                        // Convert all the document snapshot to the Student object using the data model class.
                        val studentObject = student.toObject(Student::class.java)!!
                        eligibleStudents.add(studentObject)
                    }
                    activity.getEligibleStudentsSuccess(
                        eligibleStudents,
                        company.name
                    )
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while fetching eligible students.",
                        e
                    )
                }
        } else {
            mFireStore.collection(Constants.STUDENTS)
                .whereEqualTo(Constants.COLLEGE_CODE, collegeCode)
                .whereIn(
                    Constants.BRANCH,
                    company.branchesAllowed
                )
                .whereGreaterThanOrEqualTo(Constants.CGPA, company.cgpaCutOff)
                .whereEqualTo(Constants.PLACED_ABOVE_THRESHOLD, 0)
                .get()
                .addOnSuccessListener { studentDocuments ->
                    var eligibleStudents: ArrayList<Student> = ArrayList()
                    for (student in studentDocuments) {
                        // Convert all the document snapshot to the Student object using the data model class.
                        val studentObject = student.toObject(Student::class.java)!!
                        eligibleStudents.add(studentObject)
                    }
                    activity.getEligibleStudentsSuccess(
                        eligibleStudents,
                        company.name
                    )
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while fetching eligible students.",
                        e
                    )
                }
        }
    }

    fun updateCompanyInStudentDatabase(
        studentsList: ArrayList<String>,
        companyLastRoundObject: CompanyNameAndLastRound,
        activity: NewCompanyDetailsActivity
    ) {
        for (id in studentsList) {
            mFireStore.collection(Constants.STUDENTS)
                .document(id)
                .update(
                    Constants.COMPANY_NAME_AND_LAST_ROUND,
                    FieldValue.arrayUnion(companyLastRoundObject)
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        companyLastRoundObject.lastRound -= 1
                        mFireStore.collection(Constants.STUDENTS)
                            .document(id)
                            .update(
                                Constants.COMPANY_NAME_AND_LAST_ROUND,
                                FieldValue.arrayRemove(companyLastRoundObject)
                            )
                            .addOnFailureListener { e ->
                                Log.e(
                                    activity.javaClass.simpleName,
                                    "Error while updating company in student database.",
                                    e
                                )
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while updating company in student database.",
                        e
                    )
                }
        }
        activity.updateCompanyInStudentDatabaseSuccess()
    }

    fun getSpecificCompaniesDetailsFromDatabase(
        companyNames: ArrayList<String>,
        collegeCode: String,
        roundsOver: Int,
        activity: MainActivity
    ) {
        if (companyNames.size == 0) {
            activity.populateRecyclerView(ArrayList())
            return
        }
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .collection(Constants.COMPANIES)
            .whereIn(Constants.NAME, companyNames)
            .whereEqualTo(Constants.ROUNDS_OVER, roundsOver)
            .get()
            .addOnSuccessListener { companyDocuments ->
                val companyObjects: ArrayList<Company> = ArrayList()
                for (companyDocument in companyDocuments) {
                    val companyObject = companyDocument.toObject(Company::class.java)
                    companyObjects.add(companyObject)
                }
                activity.populateRecyclerView(companyObjects)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while fetching companies from database.",
                    e
                )
            }
    }

    fun getAllCompaniesDetailsFromDatabase(
        collegeCode: String,
        roundsOver: Int,
        activity: MainActivity
    ) {
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .collection(Constants.COMPANIES)
            .whereEqualTo(Constants.ROUNDS_OVER, roundsOver)
            .get()
            .addOnSuccessListener { companyDocuments ->
                val companyObjects: ArrayList<Company> = ArrayList()
                for (companyDocument in companyDocuments) {
                    val companyObject = companyDocument.toObject(Company::class.java)
                    companyObjects.add(companyObject)
                }
                activity.populateRecyclerView(companyObjects)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while fetching companies from database.",
                    e
                )
            }
    }

    /** Get college names from the database */
    fun getCollegeNames(activity : UpdateProfileActivity){
        mFireStore.collection(Constants.COLLEGES)
            .get()
            .addOnSuccessListener { collegeDocuments ->
                var collegeNames = ArrayList<String>()
                for(document in collegeDocuments){
                    collegeNames.add(document.id)
                }
                activity.getCollegeNamesSuccess(collegeNames)
            }
    }

}