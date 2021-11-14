package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable

data class College(
    var collegeName : String ="",
    var companiesList : HashMap<String,Company>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readSerializable()!! as HashMap<String, Company>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(collegeName)
        parcel.writeSerializable(companiesList)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<College> {
        override fun createFromParcel(parcel: Parcel): College {
            return College(parcel)
        }

        override fun newArray(size: Int): Array<College?> {
            return arrayOfNulls(size)
        }
    }
}