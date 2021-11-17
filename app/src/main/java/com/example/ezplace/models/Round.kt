package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class Round(
    var number : Int,
    var date: String,
    var time : String,
    var selectedStudents : ArrayList<String>,
    var notSelectedStudents : ArrayList<String>,
    var isOver : Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readArrayList(null)!! as ArrayList<String>,
        parcel.readArrayList(null)!! as ArrayList<String>,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeInt(number)
        parcel.writeString(date)
        parcel.writeString(time)
        parcel.writeList(selectedStudents)
        parcel.writeList(notSelectedStudents)
        parcel.writeInt(isOver)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Round> {
        override fun createFromParcel(parcel: Parcel): Round {
            return Round(parcel)
        }

        override fun newArray(size: Int): Array<Round?> {
            return arrayOfNulls(size)
        }
    }
}