package com.example.ezplace.activities

import android.os.Bundle
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Round
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_view_results.*
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.example.ezplace.R
import kotlinx.android.synthetic.main.activity_declare_results.*

class ViewResultsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_results)
        setupActionBar(toolbar_view_results)

        /** gte round details from intent */
        val round = intent.getParcelableExtra<Round>(Constants.ROUND)!!

        if(round.selectedStudents.size >0){
            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().getStudentsFromIds(round.selectedStudents,this)
        }
        else{
            table_view_results.visibility = View.GONE
            tv_top_view_results.visibility = View.VISIBLE
            tv_top_view_results.text = getString(R.string.no_students_selected_from_previous_round)
        }
    }

    /** this function is called from firestore class
     * after students details are fetched successfully
     */
    fun setUpUI(selectedStudents : ArrayList<Student>){
        hideProgressDialog()
        /** This text view is used to copy its layout
         * parameters into the new text view we'll add now */
        val textView : TextView = findViewById(R.id.tv_roll_table_heading)
        /** Sort the students' list first by roll number then by name */
        val sortedStudents = selectedStudents.sortedWith(compareBy<Student> { it.rollNumber }.thenBy { it.firstName })

        for(student in sortedStudents){
            val tableRow = TableRow(this)

            /** Set a textview for roll number */
            val rollTextView = TextView(this)
            rollTextView.text = student.rollNumber
            rollTextView.textSize = 20F
            rollTextView.layoutParams = textView.layoutParams
            rollTextView.gravity = Gravity.CENTER

            /** Set a text view for name of student*/
            val nameTextView= TextView(this)
            val name = student.firstName + " " +student.lastName
            nameTextView.text = name
            nameTextView.textSize = 20F
            nameTextView.layoutParams = textView.layoutParams
            nameTextView.background = ContextCompat.getDrawable(this, R.drawable.table_cell_background)
            nameTextView.gravity = Gravity.CENTER

            /** Add roll and name textviews to the the current row */
            tableRow.addView(rollTextView)
            tableRow.addView(nameTextView)
            tableRow.background = ContextCompat.getDrawable(this, R.drawable.table_cell_background)

            table_view_results.addView(tableRow)
        }
    }
}