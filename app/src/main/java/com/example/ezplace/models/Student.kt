package com.example.ezplace.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Student(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var rollNumber: String = "",
    var cgpa: Double = 0.0,
    var collegeCode: String = "",
    var branch: String = "",
    var placed: Int = 0,
    var placedCompanyName : String="",
    var fcmToken: String = "",
    var numberOfBacklogs: Int = 0,
    var companiesListAndLastRound:
    ArrayList<CompanyNameAndLastRound> = ArrayList()
) : Parcelable


