package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable

data class College(
    var collegeName : String ="",
    var updateProfileButtonEnabled : Int =1,
    var companiesList : HashMap<String,Company> = HashMap()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readSerializable()!! as HashMap<String, Company>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(collegeName)
        parcel.writeInt(updateProfileButtonEnabled)
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