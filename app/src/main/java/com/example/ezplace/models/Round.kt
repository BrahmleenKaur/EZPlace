package com.example.ezplace.models

import android.os.Parcel
import android.os.Parcelable
import kotlin.collections.ArrayList

data class Round(
    var number : Int=0,
    var name : String ="",
    var date: Long=0,
    var time : String ="",
    var venue : String ="",
    var selectedStudents : ArrayList<String> = ArrayList(),
    var notSelectedStudents : ArrayList<String> = ArrayList(),
    var isOver : Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readLong()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readArrayList(null)!! as ArrayList<String>,
        parcel.readArrayList(null)!! as ArrayList<String>,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel){
        parcel.writeInt(number)
        parcel.writeString(name)
        parcel.writeLong(date)
        parcel.writeString(time)
        parcel.writeString(venue)
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