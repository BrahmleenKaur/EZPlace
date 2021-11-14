package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable
import kotlin.collections.ArrayList

data class Company(
    var name: String = "",
    var cgpaCutOff : Double ,
    var backLogsAllowed : Int,
    var branchesAllowed : ArrayList<String>,
    var ctcDetails : String ="",
    var location : String="",
    var deadlineToApply : String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble()!!,
        parcel.readInt()!!,
        parcel.readArrayList(null)!! as ArrayList<String>,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(name)
        parcel.writeDouble(cgpaCutOff)
        parcel.writeInt(backLogsAllowed)
        parcel.writeList(branchesAllowed)
        parcel.writeString(location)
        parcel.writeString(deadlineToApply)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Company> {
        override fun createFromParcel(parcel: Parcel): Company {
            return Company(parcel)
        }

        override fun newArray(size: Int): Array<Company?> {
            return arrayOfNulls(size)
        }
    }
}