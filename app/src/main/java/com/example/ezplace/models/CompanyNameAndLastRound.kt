package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class CompanyNameAndLastRound(
    var companyName : String="",
    var lastRound :Int=1,
    var lastRoundCleared : Int =1 /** 0 for not cleared, 1 for cleared, 2 for pending **/
) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        parcel.writeString(companyName)
        parcel.writeInt(lastRound)
        parcel.writeInt(lastRoundCleared)
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