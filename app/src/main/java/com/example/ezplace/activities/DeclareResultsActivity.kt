package com.example.ezplace.activities

import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Round
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_declare_results.*
import kotlinx.android.synthetic.main.activity_view_results.*

class DeclareResultsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declare_results)

        setupActionBar(toolbar_declare_results)

        val round = intent.getParcelableExtra<Round>(Constants.ROUND)!!
        val secondLastRound = intent.getParcelableExtra<Round>(Constants.SECOND_LAST_ROUND)!!

        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getStudentsFromIds(secondLastRound.selectedStudents,this)
    }

    fun setUpUI(selectedStudents : ArrayList<Student>){
        hideProgressDialog()

        val textView : TextView = findViewById(R.id.tv_roll_table_heading_declare)

        val sortedStudents : List<Student> = selectedStudents.sortedWith(compareBy<Student> { it.rollNumber }.thenBy { it.firstName })
        for(student in sortedStudents){
            Log.i("tag",student.firstName)
            val tableRow = TableRow(this)

            val rollTextView = TextView(this)
            rollTextView.text = student.rollNumber
            rollTextView.textSize = 20F
            rollTextView.layoutParams = textView.layoutParams
            rollTextView.setTextColor(ContextCompat.getColor(this,R.color.white))
            rollTextView.background = ContextCompat.getDrawable(this, R.drawable.table_cell_background)

            val nameTextView= TextView(this)
            val name = student.firstName + " " +student.lastName
            nameTextView.text = name
            nameTextView.textSize = 20F
            nameTextView.layoutParams = textView.layoutParams
            nameTextView.setTextColor(ContextCompat.getColor(this,R.color.white))
            nameTextView.background = ContextCompat.getDrawable(this, R.drawable.table_cell_background)

            val checkBox = CheckBox(this)

            tableRow.addView(rollTextView)
            tableRow.addView(nameTextView)
            tableRow.addView(checkBox)

            table_declare_results.addView(tableRow)
        }

        getSelectedStudentsFromUI(sortedStudents)
    }

    private fun getSelectedStudentsFromUI(students : List<Student>) {
        val selectedStudentsIndices =ArrayList<Int>()
        for(index in students.indices){
            // if check box checked, add index to array
        }
        // now from these indices get the details from the students array and update and notify
    }
}