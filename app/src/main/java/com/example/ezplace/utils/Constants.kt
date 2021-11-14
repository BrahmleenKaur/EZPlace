package com.example.ezplace.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    // Firebase Constants
    // This  is used for the collection name for USERS.
    const val STUDENTS: String = "students"
    const val COLLEGES: String = "colleges"
    const val TPO: String = "tpo"
    const val COMPANIES: String = "companies"

    // Firebase database field names
    const val ID = "id"
    const val FIRST_NAME: String = "firstName"
    const val LAST_NAME: String = "lastName"
    const val COLLEGE_CODE: String = "collegeCode"
    const val COLLEGE_NAME = "collegeName"
    const val BRANCH: String = "branch"
    const val CGPA: String = "cgpa"
    const val ROLL_NUMBER ="rollNumber"
    const val BACKLOGS_ALLOWED = "backLogsAllowed"
    const val IS_PLACED_ABOVE_THRESHOLD ="isPlacedAboveThreshold"

    const val STUDENT_DETAILS: String = "studentDetails"
    const val TPO_DETAILS: String = "tpoDetails"
    const val IS_STUDENT: String = "isStudent"

    const val EZ_PLACE_PREFERENCES = "EZPlacePreferences"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"

    const val FCM_BASE_URL: String = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION: String = "authorization"
    const val FCM_KEY: String = "key"
    const val FCM_SERVER_KEY: String =
        "AAAAwZmF2xs:APA91bG2-LxrrvK_12uGtm7uKhVReNG0tQUBBFGm7UzOVOtHhMeSBoc1aeywvNg8ZKlvCSXd3VMW7ZCq1wTtUv1-fBnUgSn2V-edYm6pSTFjlcMFMnyXl-ZW_SEcUkCJ90GqyWhakN-M"
    const val FCM_KEY_TITLE: String = "title"
    const val FCM_KEY_MESSAGE: String = "message"
    const val FCM_KEY_DATA: String = "data"
    const val FCM_KEY_TO: String = "to"
    const val POST: String = "POST"

    // Branches list
    val ALL_BRANCHES = arrayOf(
        "Computer Science", "Electronics and Communication", "Electrical",
        "Instrumentation and Control", "Mechanical", "Chemical", "Textile"
    )

}