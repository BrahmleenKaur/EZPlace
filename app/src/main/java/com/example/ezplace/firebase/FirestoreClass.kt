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

    /** the email is searched in the tpo database
     * if found, he/she is signed in, else the user
     * is a student, so will be sent to loadStudentData function */
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

    /** fetch companies in which student is eligible.
     * Used in late registration */
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
                    val companies = ArrayList<Company>()
                    for (companyDoc in companyDocuments) {
                        val companyObject = companyDoc.toObject(Company::class.java)
                        if (companyObject.cgpaCutOff >= student.cgpa)
                            companies.add(companyObject)
                    }
                    activity.getEligibleCompaniesNamesSuccess(companies)
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
                    val companies = ArrayList<Company>()
                    for (companyDoc in companyDocuments) {
                        val companyObject = companyDoc.toObject(Company::class.java)
                        if (companyObject.cgpaCutOff <= student.cgpa)
                            companies.add(companyObject)
                    }
                    activity.getEligibleCompaniesNamesSuccess(companies)
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
        val companyHashMap = HashMap<String, Company>()
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

    /** Fetch students who meet the company's criteria */
    fun getEligibleStudents(
        company: Company,
        collegeCode: String,
        activity: NewCompanyDetailsActivity
    ) {
        if (company.backLogsAllowed == 0) {
            mFireStore.collection(Constants.STUDENTS)
                .whereEqualTo(Constants.COLLEGE_CODE, collegeCode)
                .whereEqualTo(Constants.PLACED, 0)
                .whereIn(
                    Constants.BRANCH,
                    company.branchesAllowed
                )
                .whereGreaterThanOrEqualTo(Constants.CGPA, company.cgpaCutOff)
                .whereEqualTo(Constants.NUMBER_OF_BACKLOGS, company.backLogsAllowed)
                .get()
                .addOnSuccessListener { studentDocuments ->
                    val eligibleStudents: ArrayList<Student> = ArrayList()
                    for (student in studentDocuments) {
                        // Convert all the document snapshot to the Student object using the data model class.
                        val studentObject = student.toObject(Student::class.java)
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
                .whereEqualTo(Constants.PLACED, 0)
                .whereIn(
                    Constants.BRANCH,
                    company.branchesAllowed
                )
                .whereGreaterThanOrEqualTo(Constants.CGPA, company.cgpaCutOff)
                .get()
                .addOnSuccessListener { studentDocuments ->
                    val eligibleStudents: ArrayList<Student> = ArrayList()
                    for (student in studentDocuments) {
                        // Convert all the document snapshot to the Student object using the data model class.
                        val studentObject = student.toObject(Student::class.java)
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

    /** the company status is changes in all the students documents
     * This is done using recursion, because firestore queries are slower
     * than for loops, so inconsistency may occur
     * Therefore, the next function call is made in
     * the onSuccessListener
     */
    fun updateCompanyStatusInStudentDatabase(
        index: Int,
        studentsList: ArrayList<String>,
        companyLastRoundObject: CompanyNameAndLastRound,
        previousCompanyLastRoundObject: CompanyNameAndLastRound,
        activity: Activity,
        selectedStudentsUpdated: Boolean,
        updatePlacedField: Boolean,
        placedCompanyName : String =""
    ) {
        if (index == studentsList.size) {
            when (activity) {
                is NewCompanyDetailsActivity -> {
                    if (selectedStudentsUpdated) {
                        activity.notEligibleStudentsDatabaseSuccess()
                    } else {
                        activity.updateCompanyInStudentDatabaseSuccess(studentsList)
                    }
                }
                is AddRoundActivity -> {
                    if (selectedStudentsUpdated) {
                        activity.updateNotEligibleStudentsDatabaseSuccess()
                    } else {
                        activity.updateEligibleStudentsDatabaseSuccess()
                    }
                }
                is DeclareResultsActivity -> {
                    if (selectedStudentsUpdated) {
                        activity.notSelectedStudentsDatabaseUpdatedSuccess()
                    } else {
                        activity.selectedStudentsDatabaseUpdatedSuccess()
                    }
                }
            }
            return
        }

        /** update "placed" field or not
         * This is done to make sure, a student who is already placed
         * his data does not get overwritten */
        if (updatePlacedField) {
            /** to update an element in an array, first add new element, then
             * remove previous element */
            mFireStore.collection(Constants.STUDENTS)
                .document(studentsList[index])
                .update(
                    mapOf(
                        Constants.COMPANY_NAME_AND_LAST_ROUND to FieldValue.arrayUnion(
                            companyLastRoundObject
                        ),
                        Constants.PLACED to 1,
                        Constants.PLACED_COMPANY_NAME to placedCompanyName
                    )
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mFireStore.collection(Constants.STUDENTS)
                            .document(studentsList[index])
                            .update(
                                Constants.COMPANY_NAME_AND_LAST_ROUND,
                                FieldValue.arrayRemove(previousCompanyLastRoundObject)
                            )
                            .addOnSuccessListener {
                                updateCompanyStatusInStudentDatabase(
                                    index + 1,
                                    studentsList,
                                    companyLastRoundObject,
                                    previousCompanyLastRoundObject,
                                    activity,
                                    selectedStudentsUpdated,
                                    updatePlacedField
                                )
                            }
                            .addOnFailureListener { e ->
                                when (activity) {
                                    is AddRoundActivity -> {
                                        activity.hideProgressDialog()
                                    }
                                    is NewCompanyDetailsActivity -> {
                                        activity.hideProgressDialog()
                                    }
                                    is DeclareResultsActivity -> {
                                        activity.hideProgressDialog()
                                    }
                                }
                                Log.e(
                                    activity.javaClass.simpleName,
                                    "Error while updating company in student database.",
                                    e
                                )
                            }
                    }
                }
                .addOnFailureListener { e ->
                    when (activity) {
                        is AddRoundActivity -> {
                            activity.hideProgressDialog()
                        }
                        is NewCompanyDetailsActivity -> {
                            activity.hideProgressDialog()
                        }
                        is DeclareResultsActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while updating company in student database.",
                        e
                    )
                }
        } else {
            mFireStore.collection(Constants.STUDENTS)
                .document(studentsList[index])
                .update(
                    Constants.COMPANY_NAME_AND_LAST_ROUND,
                    FieldValue.arrayUnion(companyLastRoundObject)
                )
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        mFireStore.collection(Constants.STUDENTS)
                            .document(studentsList[index])
                            .update(
                                Constants.COMPANY_NAME_AND_LAST_ROUND,
                                FieldValue.arrayRemove(previousCompanyLastRoundObject)
                            )
                            .addOnSuccessListener {
                                updateCompanyStatusInStudentDatabase(
                                    index + 1,
                                    studentsList,
                                    companyLastRoundObject,
                                    previousCompanyLastRoundObject,
                                    activity,
                                    selectedStudentsUpdated,
                                    updatePlacedField
                                )
                            }
                            .addOnFailureListener { e ->
                                when (activity) {
                                    is AddRoundActivity -> {
                                        activity.hideProgressDialog()
                                    }
                                    is NewCompanyDetailsActivity -> {
                                        activity.hideProgressDialog()
                                    }
                                    is DeclareResultsActivity -> {
                                        activity.hideProgressDialog()
                                    }
                                }
                                Log.e(
                                    activity.javaClass.simpleName,
                                    "Error while updating company in student database.",
                                    e
                                )
                            }
                    }
                }
                .addOnFailureListener { e ->
                    when (activity) {
                        is AddRoundActivity -> {
                            activity.hideProgressDialog()
                        }
                        is NewCompanyDetailsActivity -> {
                            activity.hideProgressDialog()
                        }
                        is DeclareResultsActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while updating company in student database.",
                        e
                    )
                }
        }

    }

    fun getSpecificCompaniesDetailsFromDatabase(
        companyNames: ArrayList<String>,
        collegeCode: String,
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
        activity: Activity
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
                when (activity) {
                    is MainActivity -> {
                        activity.populateRecyclerView(companyObjects)
                    }
                    is PlacementsRecordsActivity -> {
                        activity.populateRecyclerView(companyObjects)
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is PlacementsRecordsActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while fetching companies from database.",
                    e
                )
            }
    }

    fun getCollegeNames(activity: UpdateProfileActivity) {
        mFireStore.collection(Constants.COLLEGES)
            .get()
            .addOnSuccessListener { collegeDocuments ->
                val collegeNames = ArrayList<String>()
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
            .whereEqualTo(Constants.COLLEGE_CODE, collegeCode)
            .get()
            .addOnSuccessListener { studentDocs ->
                val studentsList = ArrayList<Student>()
                for (student in studentDocs) {
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
        if(eligibleStudentsIds.size == 0 ){
            when (activity) {
                is AddRoundActivity -> {
                    activity.getStudentsFromIdsSuccess(ArrayList())
                }
                is ViewResultsActivity -> {
                    activity.setUpUI(ArrayList())
                }
                is DeclareResultsActivity -> {
                    activity.setUpUI(ArrayList())
                }
                is PlacedStudentsActivity -> {
                    activity.setUpUI(ArrayList())
                }
            }
            return
        }
        mFireStore.collection(Constants.STUDENTS)
            .whereIn(Constants.ID, eligibleStudentsIds)
            .get()
            .addOnSuccessListener { studentDocs ->
                val studentsList = ArrayList<Student>()
                for (student in studentDocs) {
                    studentsList.add(student.toObject(Student::class.java))
                }
                when (activity) {
                    is AddRoundActivity -> {
                        activity.getStudentsFromIdsSuccess(studentsList)
                    }
                    is ViewResultsActivity -> {
                        activity.setUpUI(studentsList)
                    }
                    is DeclareResultsActivity -> {
                        activity.setUpUI(studentsList)
                    }
                    is PlacedStudentsActivity -> {
                        activity.setUpUI(studentsList)
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is AddRoundActivity -> {
                        activity.hideProgressDialog()
                    }
                    is ViewResultsActivity -> {
                        activity.hideProgressDialog()
                    }
                    is DeclareResultsActivity -> {
                        activity.hideProgressDialog()
                    }
                    is PlacedStudentsActivity -> {
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

    fun updateCompanyInCollegeDatabase(
        companyHashMap: HashMap<String, Any>,
        companyName: String,
        collegeCode: String,
        activity: Activity
    ) {
        mFireStore.collection(Constants.COLLEGES)
            .document(collegeCode)
            .collection(Constants.COMPANIES)
            .document(companyName)
            .set(companyHashMap, SetOptions.merge())
            .addOnSuccessListener {
                when (activity) {
                    is AddRoundActivity -> {
                        activity.updateCompanyDatabaseSuccess()
                    }
                    is DeclareResultsActivity -> {
                        activity.companyDatabaseUpdatedSuccess()
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is AddRoundActivity -> {
                        activity.hideProgressDialog()
                    }
                    is DeclareResultsActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while updating company in college",
                    e
                )
            }
    }

}