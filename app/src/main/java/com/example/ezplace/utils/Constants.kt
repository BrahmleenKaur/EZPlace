package com.example.ezplace.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val GROUP_KEY: String = "EZ_Place_Group_key"
    const val PLACED_COMPANY_NAME: String ="placedCompanyName"
    const val IS_PR: String ="is pr"
    const val STUDENT_EMAIL: String ="student email"
    const val STUDENT_PASSWORD ="student password"
    const val PR_EMAIL ="tpo email"
    const val PR_PASSWORD ="tpo password"
    const val COMPANY: String ="Company"
    const val SECOND_LAST_ROUND: String = "Second last round"
    const val ROUND: String ="round"
    const val SCREENING_ROUND: String= "Screening round"
    const val OVER = "OVER"
    const val NOT_CLEARED ="NOT CLEARED"
    const val PENDING = " PENDING"
    const val CLEARED ="CLEARED"
    const val ROUNDS_LIST ="roundsList"
    const val COMPANY_DETAIL: String = "Company Details"
    const val DECLARE_RESULTS = "Declare results"
    const val VIEW_RESULTS = "View results"

    // Firebase Constants
    // This  is used for the collection names used in firestore
    const val STUDENTS: String = "students"
    const val COLLEGES: String = "colleges"
    const val TPO: String = "tpo"
    const val COMPANIES: String = "companies"

    // Firebase database field names
    const val ID = "id"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val COLLEGE_CODE: String = "collegeCode"
    const val BRANCH: String = "branch"
    const val CGPA: String = "cgpa"
    const val ROLL_NUMBER ="rollNumber"
    const val BACKLOGS_ALLOWED = "backLogsAllowed"
    const val PLACED ="placed"
    const val NAME="name"
    const val COMPANY_NAME_AND_LAST_ROUND = "companiesListAndLastRound"
    const val NUMBER_OF_BACKLOGS = "numberOfBacklogs"
    const val ROUNDS_OVER = "roundsOver"
    const val SELECT_COLLEGE_NAME = "Select college name"
    const val IS_UPDATE_BUTTON_ENABLED: String="updateProfileButtonEnabled"
    const val BRANCHES_ALLOWED = "branchesAllowed"
    const val DEADLINE_TO_APPLY="deadlineToApply"

    const val STUDENT_DETAILS: String = "studentDetails"
    const val TPO_DETAILS: String = "tpoDetails"
    const val IS_STUDENT: String = "isStudent"

    const val EZ_PLACE_PREFERENCES = "EZPlacePreferences"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY: String = "key"
    const val FCM_SERVER_KEY: String = "******"
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"
    const val FCM_KEY_DETAILED_MESSAGE : String = "detailed_message"
    const val POST: String = "POST"

    // Branches list
    val ALL_BRANCHES = arrayOf(
        "Computer Science", "Information Technology", "Electronics and Communication", "Electrical",
        "Instrumentation and Control", "Mechanical", "Chemical", "Textile"
    )

}
