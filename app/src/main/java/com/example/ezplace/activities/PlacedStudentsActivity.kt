package com.example.ezplace.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_placed_students.*

class PlacedStudentsActivity : BaseActivity() {

    private lateinit var company: Company
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placed_students)
        setupActionBar(toolbar_placed_students)

        /** get company from intent */
        company = intent.getParcelableExtra<Company>(Constants.COMPANY_DETAIL)!!
        /** Set the title in the toolbar */
        toolbar_placed_students.title = "${company.name} final selects"

        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getStudentsFromIds(company.roundsList.last().selectedStudents, this)
    }

    /** show the list of placed students for selected company in the UI */
    fun setUpUI(students: ArrayList<Student>) {
        hideProgressDialog()

        if(students.isEmpty()){
            tv_congratulations_placed_students.visibility = View.GONE
            list_view_placed_students.visibility = View.GONE
            tv_no_student_cleared_placed_students.visibility = View.VISIBLE
            val message = "No student has been selected in ${company.name}."
            tv_no_student_cleared_placed_students.text = message
        }
        else{
            tv_no_student_cleared_placed_students.visibility = View.GONE
            val text = "Congratulations to the students for getting placed in ${company.name}."
            tv_congratulations_placed_students.text = text

            /** Create a list to show in UI */
            val studentsNamesAndRollNumbers = ArrayList<String>()
            for (student in students) {
                val text = student.rollNumber + " " + student.firstName + " " + student.lastName
                studentsNamesAndRollNumbers.add(text)
            }

            val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                studentsNamesAndRollNumbers
            )
            list_view_placed_students.adapter = arrayAdapter
        }
    }
}