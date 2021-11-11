package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable

data class Student(
    var id: String ="",
    var firstName: String = "",
    var lastName: String = "",
    var email: String ="",
    var mobile: Long = 0,
    var cgpa: Double =0.0,
    var collegeCode: String="",
    var branchCode: String= "",
    var isPlacedAboveThreshold: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong()!!,
        parcel.readDouble()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeLong(mobile)
        parcel.writeDouble(cgpa)
        parcel.writeString(collegeCode)
        parcel.writeString(branchCode)
        parcel.writeInt(isPlacedAboveThreshold)
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