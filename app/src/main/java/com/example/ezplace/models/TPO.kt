package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable

data class TPO(
    var id: String ="",
    var firstName: String = "",
    var lastName: String = "",
    var email: String ="",
    var collegeCode: String=""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeString(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeString(collegeCode)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<TPO> {
        override fun createFromParcel(parcel: Parcel): TPO {
            return TPO(parcel)
        }

        override fun newArray(size: Int): Array<TPO?> {
            return arrayOfNulls(size)
        }
    }
}