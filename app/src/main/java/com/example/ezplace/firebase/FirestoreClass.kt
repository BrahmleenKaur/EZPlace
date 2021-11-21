package com.example.ezplace.firebase

import android.app.Activity
import android.util.Log
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
                when (activity) {
                    is SignUpActivity -> {
                        // Here call a function of base activity for transferring the result to it.
                        activity.tpoRegisteredSuccess(tpoInfo)
                    }
                    is AddPrActivity -> {
                        activity.addPrSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignUpActivity -> {
                        // Here call a function of base activity for transferring the result to it.
                        activity.hideProgressDialog()
                    }
                    is AddPrActivity -> {
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

    fun updateCollege(
        collegeCode: String,
        collegeHashmap: HashMap<String, Int>,
        activity: MainActivity
    ) {
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

    fun getCollege(collegeCode: String, activity: UpdateProfileActivity) {
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .get()
            .addOnSuccessListener { document ->
                var isUpdateButtonEnabled: Long = 1
                if (document.exists())
                    isUpdateButtonEnabled =
                        document[Constants.IS_UPDATE_BUTTON_ENABLED] as Long
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
                val loggedInStudent = document.toObject(Student::class.java)!!

                // Here call a function of base activity for transferring the result to it.
                when (activity) {
                    is RoundDetailsActivity -> {
                        activity.loadStudentDataSuccess(loggedInStudent)
                    }
                    is MainActivity -> {
                        activity.updateStudentDetailsSuccess(loggedInStudent)
                    }
                    is SplashActivity -> {
                        activity.signInSuccessByStudent(loggedInStudent)
                    }
                    is SignInActivity -> {
                        activity.signInSuccessByStudent(loggedInStudent)
                    }
                    is UpdateProfileActivity -> {
                        activity.setStudentDataInUI(loggedInStudent)
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
                    "Error while getting loggedIn student details",
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

    fun getEligibleCompaniesNamesForOneStudent(student: Student, activity: UpdateProfileActivity) {
        if (student.numberOfBacklogs > 0) {
            mFireStore.collection(Constants.COLLEGES)
                .document(student.collegeCode)
                .collection(Constants.COMPANIES)
                .whereLessThanOrEqualTo(Constants.DEADLINE_TO_APPLY, System.currentTimeMillis())
                .whereArrayContains(Constants.BRANCHES_ALLOWED, student.branch)
                .whereEqualTo(Constants.BACKLOGS_ALLOWED, 1)
                .get()
                .addOnSuccessListener { companyDocuments ->
                    var companyNames = ArrayList<String>()
                    for (companyDoc in companyDocuments) {
                        val companyObject = companyDoc.toObject(Company::class.java)
                        if (companyObject.cgpaCutOff >= student.cgpa)
                            companyNames.add(companyObject.name)
                    }
                    activity.getEligibleCompaniesNamesSuccess(companyNames)
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while getting eligible companies",
                        e
                    )
                }
        } else {
            mFireStore.collection(Constants.COLLEGES)
                .document(student.collegeCode)
                .collection(Constants.COMPANIES)
                .whereGreaterThanOrEqualTo(Constants.DEADLINE_TO_APPLY, System.currentTimeMillis())
                .whereArrayContains(Constants.BRANCHES_ALLOWED, student.branch)
                .get()
                .addOnSuccessListener { companyDocuments ->
                    var companyNames = ArrayList<String>()
                    for (companyDoc in companyDocuments) {
                        val companyObject = companyDoc.toObject(Company::class.java)
                        if (companyObject.cgpaCutOff <= student.cgpa)
                            companyNames.add(companyObject.name)
                    }
                    activity.getEligibleCompaniesNamesSuccess(companyNames)
                }
                .addOnFailureListener { e ->
                    activity.hideProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while getting eligible companies",
                        e
                    )
                }
        }
    }

    fun updateStudentProfileData(activity: Activity, studentHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.STUDENTS) // Collection Name
            .document(FirebaseAuthClass().getCurrentUserID()) // Document ID
            .update(studentHashMap) // A hashmap of fields which are to be updated.
            .addOnSuccessListener {
                // Profile data is updated successfully.
                Log.i(activity.javaClass.simpleName, "Profile Data updated successfully!")
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
                activity.companyRegisteredSuccess()
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
        activity: Activity
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
                    if(activity is AddRoundActivity) activity.hideProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while updating company in student database.",
                        e
                    )
                }
        }
        when(activity){
            is NewCompanyDetailsActivity->{
                activity.updateCompanyInStudentDatabaseSuccess(studentsList)
            }
            is AddRoundActivity ->{
                activity.updateStudentsDatabaseSuccess()
            }
        }

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
    fun getCollegeNames(activity: UpdateProfileActivity) {
        mFireStore.collection(Constants.COLLEGES)
            .get()
            .addOnSuccessListener { collegeDocuments ->
                var collegeNames = ArrayList<String>()
                for (document in collegeDocuments) {
                    collegeNames.add(document.id)
                }
                activity.getCollegeNamesSuccess(collegeNames)
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

    fun addRoundInCompany(
        round: Round,
        companyName: String,
        collegeCode: String,
        activity: AddRoundActivity
    ) {
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .collection(Constants.COMPANIES)
            .document(companyName)
            .update(Constants.ROUNDS_LIST, FieldValue.arrayUnion(round))
            .addOnSuccessListener {
                activity.addRoundInCompanySuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while adding new round",
                    e
                )
            }
    }

    fun loadCompany(companyName: String, collegeCode: String, activity: RoundDetailsActivity) {
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .collection(Constants.COMPANIES)
            .document(companyName)
            .get()
            .addOnSuccessListener { companyDoc ->
                val companyOBject = companyDoc.toObject(Company::class.java)
                if (companyOBject != null) {
                    activity.loadCompanySuccess(companyOBject)
                }
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while a loading company data",
                    e
                )
            }
    }

    fun getAllStudents(collegeCode: String, activity: NewCompanyDetailsActivity) {
        mFireStore.collection(Constants.STUDENTS)
            .whereEqualTo(Constants.COLLEGE_CODE,collegeCode)
            .get()
            .addOnSuccessListener { studentDocs ->
                val studentsList = ArrayList<Student>()
                for(student in studentDocs){
                    studentsList.add(student.toObject(Student::class.java))
                }
                activity.getAllStudentsSuccess(studentsList)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while fetching all students",
                    e
                )
            }
    }

    fun getStudentsFromIds(eligibleStudentsIds: ArrayList<String>, activity: Activity) {
        mFireStore.collection(Constants.STUDENTS)
            .whereIn(Constants.ID,eligibleStudentsIds)
            .get()
            .addOnSuccessListener { studentDocs ->
                val studentsList = ArrayList<Student>()
                for(student in studentDocs){
                    studentsList.add(student.toObject(Student::class.java))
                }
                when(activity){
                    is AddRoundActivity ->{
                        activity.getStudentsFromIdsSuccess(studentsList)
                    }
                    is ViewResultsActivity ->{
                        activity.setUpUI(studentsList)
                    }
                    is DeclareResultsActivity ->{
                        activity.setUpUI(studentsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                when(activity){
                    is AddRoundActivity ->{
                        activity.hideProgressDialog()
                    }
                    is ViewResultsActivity ->{
                        activity.hideProgressDialog()
                    }
                    is DeclareResultsActivity ->{
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while fetching students from ids",
                    e
                )
            }
    }

}