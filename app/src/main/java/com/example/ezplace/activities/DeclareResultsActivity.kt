package com.example.ezplace.activities

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.CompanyNameAndLastRound
import com.example.ezplace.models.Round
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_declare_results.*

class DeclareResultsActivity : BaseActivity() {

    private lateinit var round: Round
    private lateinit var company: Company
    private var collegeCode = ""
    var selectedStudentsIds = ArrayList<String>()
    private val notSelectedStudentsIds = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declare_results)

        setupActionBar(toolbar_declare_results)

        round = intent.getParcelableExtra<Round>(Constants.ROUND)!!
        company = intent.getParcelableExtra<Company>(Constants.COMPANY)!!
        val secondLastRound = intent.getParcelableExtra<Round>(Constants.SECOND_LAST_ROUND)!!

        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getStudentsFromIds(secondLastRound.selectedStudents, this)
    }

    fun setUpUI(selectedStudents: ArrayList<Student>) {
        hideProgressDialog()

        val textView: TextView = findViewById(R.id.tv_roll_table_heading_declare)

        val sortedStudents: List<Student> =
            selectedStudents.sortedWith(compareBy<Student> { it.rollNumber }.thenBy { it.firstName })
        for (student in sortedStudents) {
            collegeCode = student.collegeCode
            if (student.placed == 1) continue

            val tableRow = TableRow(this)

            val rollTextView = TextView(this)
            rollTextView.text = student.rollNumber
            rollTextView.textSize = 20F
            rollTextView.layoutParams = textView.layoutParams
            rollTextView.gravity = Gravity.CENTER

            val nameTextView = TextView(this)
            val name = student.firstName + " " + student.lastName
            nameTextView.text = name
            nameTextView.textSize = 20F
            nameTextView.layoutParams = textView.layoutParams
            nameTextView.background =
                ContextCompat.getDrawable(this, R.drawable.table_cell_background)
            nameTextView.gravity = Gravity.CENTER

            tableRow.addView(rollTextView)
            tableRow.addView(nameTextView)
            tableRow.background =
                ContextCompat.getDrawable(this, R.drawable.table_cell_background)

            tableRow.setOnClickListener {
                if (tableRow.background.constantState ==
                    ContextCompat.getDrawable(this, R.drawable.table_cell_background)?.constantState
                ) {
                    tableRow.background =
                        ContextCompat.getDrawable(this, R.drawable.table_cell_background_green)
                    nameTextView.background =
                        ContextCompat.getDrawable(this, R.drawable.table_cell_background_green)
                } else {
                    tableRow.background =
                        ContextCompat.getDrawable(this, R.drawable.table_cell_background)
                    nameTextView.background =
                        ContextCompat.getDrawable(this, R.drawable.table_cell_background)
                }
            }

            table_declare_results.addView(tableRow)
        }

        btn_declare_results.setOnClickListener {
            getSelectedStudentsFromUI(sortedStudents)
        }
    }

    private fun getSelectedStudentsFromUI(students: List<Student>) {
        val selectedStudentsIndices = ArrayList<Int>()
        val notSelectedStudentsIndices = ArrayList<Int>()
        var index = 0
        for (tableRow in table_declare_results.children) {
            /** header row */
            if (index == 0) {
                index += 1
                continue
            }
            if (tableRow.background.constantState ==
                ContextCompat.getDrawable(
                    this,
                    R.drawable.table_cell_background_green
                )?.constantState
            ) {
                selectedStudentsIndices.add(index)
            } else {
                notSelectedStudentsIndices.add(index)
            }
            index += 1
        }

        /** Notify all the students regarding results
        This will be done in background using Async tasks*/
        for (index in selectedStudentsIndices) {
            val student = students[index - 1]
            selectedStudentsIds.add(student.id)
            var message = ""
            message = if (cb_declare_results.isChecked)
                "Hurray !!. You have been placed at ${company.name}."
            else
                "Congratulations ${student.firstName} on clearing ${company.name}'s ${round.name} round"
            SendNotificationToEligibleStudentsAsyncTask(message, student.fcmToken).execute()
        }

        for (index in notSelectedStudentsIndices) {
            val student = students[index - 1]
            notSelectedStudentsIds.add(student.id)
            val message = "You have not cleared the ${company.name}'s ${round.name} round"
            SendNotificationToEligibleStudentsAsyncTask(message, student.fcmToken).execute()
        }

        val updatedRound = round.copy()
        updatedRound.selectedStudents = ArrayList(selectedStudentsIds)
        updatedRound.isOver = 1

        val updatedRoundsList = ArrayList(company.roundsList)
        updatedRoundsList.removeLast()
        updatedRoundsList.add(updatedRound)

        val companyHashmap = HashMap<String, Any>()
        companyHashmap[Constants.ROUNDS_LIST] = updatedRoundsList

        if (cb_declare_results.isChecked) {
            companyHashmap[Constants.ROUNDS_OVER] = 1
        }
        updateCompanyDatabase(companyHashmap)
    }

    private fun updateCompanyDatabase(companyHashmap : HashMap<String,Any>){
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().updateCompanyInCollegeDatabase(
            companyHashmap,
            company.name,
            collegeCode,
            this
        )
    }

    fun companyDatabaseUpdatedSuccess() {

        /** Update the selected student's database */
        val companyLastRoundObject = CompanyNameAndLastRound(company.name, round.number, 1)
        val previousCompanyLastRoundObject = CompanyNameAndLastRound(company.name, round.number, 2)

        var placed = 0
        if (cb_declare_results.isChecked) {
            placed = 1
        }
        if (placed == 1) {
            FirestoreClass().updateCompanyStatusInStudentDatabase(
                index =0,
                selectedStudentsIds,
                companyLastRoundObject,
                previousCompanyLastRoundObject,
                activity = this,
                selectedStudentsUpdated = false,
                updatePlacedField = true,
                placedCompanyName = company.name
            )
        }
    }

    fun selectedStudentsDatabaseUpdatedSuccess() {

        /** Update the not selected student's database */
        val companyLastRoundObject = CompanyNameAndLastRound(company.name, round.number, 0)
        val previousCompanyLastRoundObject = CompanyNameAndLastRound(company.name, round.number, 2)

        FirestoreClass().updateCompanyStatusInStudentDatabase(
            0,
            notSelectedStudentsIds,
            companyLastRoundObject,
            previousCompanyLastRoundObject,
            activity = this,
            selectedStudentsUpdated = true,
            updatePlacedField = false
        )
    }

    fun notSelectedStudentsDatabaseUpdatedSuccess() {
        hideProgressDialog()
        finish()
    }
}