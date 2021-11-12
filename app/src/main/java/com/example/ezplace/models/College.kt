package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable

data class College(
    var collegeName : String ="",
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(collegeName)
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