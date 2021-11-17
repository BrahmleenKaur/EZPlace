package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class CompanyNameAndLastRound(
    var companyName : String="",
    var lastRound :Int=0
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(companyName)
        parcel.writeInt(lastRound)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CompanyNameAndLastRound> {
        override fun createFromParcel(parcel: Parcel): CompanyNameAndLastRound {
            return CompanyNameAndLastRound(parcel)
        }

        override fun newArray(size: Int): Array<CompanyNameAndLastRound?> {
            return arrayOfNulls(size)
        }
    }
}