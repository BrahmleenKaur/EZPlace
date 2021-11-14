package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable

data class Student(
    var id: String ="",
    var firstName: String = "",
    var lastName: String = "",
    var email: String ="",
    var mobile: Long = 0,
    var rollNumber : String ="",
    var cgpa: Double =0.0,
    var collegeCode: String="",
    var branch : String= "",
    var isPlacedAboveThreshold: Int = 0,
    var fcmToken : String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeLong(mobile)
        parcel.writeString(rollNumber)
        parcel.writeDouble(cgpa)
        parcel.writeString(collegeCode)
        parcel.writeString(branch)
        parcel.writeInt(isPlacedAboveThreshold)
        parcel.writeString(fcmToken)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Student> {
        override fun createFromParcel(parcel: Parcel): Student {
            return Student(parcel)
        }

        override fun newArray(size: Int): Array<Student?> {
            return arrayOfNulls(size)
        }
    }
}