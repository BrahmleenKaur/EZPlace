package com.example.ezplace.models

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.collections.ArrayList

data class Company(
    var name: String = "",
    var cgpaCutOff : Double ,
    var backLogsAllowed : Boolean,
    var branchesAllowed : ArrayList<String>,
    var ctcDetails : String ="",
    var location : String="",
    var deadlineToApply : String
) : Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble()!!,
        parcel.readBoolean()!!,
        parcel.readArrayList(null)!! as ArrayList<String>,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(name)
        parcel.writeDouble(cgpaCutOff)
        parcel.writeBoolean(backLogsAllowed)
        parcel.writeList(branchesAllowed)
        parcel.writeString(location)
        parcel.writeString(deadlineToApply)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Company> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Company {
            return Company(parcel)
        }

        override fun newArray(size: Int): Array<Company?> {
            return arrayOfNulls(size)
        }
    }
}