package com.example.ezplace.activities

import android.os.Bundle
import android.view.Gravity
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
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
    private var isLastRound = false
    var selectedStudentsIds = ArrayList<String>()
    private val notSelectedStudentsIds = ArrayList<String>()

    /** A hashmap to store the students indices*/
    private val studentIndexHashmap = HashMap<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_declare_results)
        setupActionBar(toolbar_declare_results)

        /** get information from intent **/
        round = intent.getParcelableExtra<Round>(Constants.ROUND)!!
        company = intent.getParcelableExtra<Company>(Constants.COMPANY)!!
        val secondLastRound = intent.getParcelableExtra<Round>(Constants.SECOND_LAST_ROUND)!!

        /** The selected students for this round will be
         * subset of the selected students of the previous round
         * so secondLast round selcted students' details are fetched
         */
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getStudentsFromIds(secondLastRound.selectedStudents, this)
    }

    /** This function is called from the firestore class,
     * after successfully getting student details
     * It displays the list of students in a table format */
    fun setUpUI(selectedStudents: ArrayList<Student>) {
        hideProgressDialog()

        /** This text view is used to copy its layout
         * parameters into the new text view we'll add now */
        val textView: TextView = findViewById(R.id.tv_roll_table_heading_declare)

        /** Sort the students' list first by roll number then by name */
        val sortedStudents: List<Student> =
            selectedStudents.sortedWith(compareBy<Student> { it.rollNumber }.thenBy { it.firstName })

        var index = 0
        for (student in sortedStudents) {
            studentIndexHashmap[student.rollNumber] = index
            index += 1
            /** set the collegeCode variable declared on top */
            collegeCode = student.collegeCode
            /** If the student is placed, do not
             * show his/her name on the screen */
            if (student.placed == 1) continue

            val tableRow = TableRow(this)

            /** Set a textview for roll number */
            val rollTextView = TextView(this)
            rollTextView.text = student.rollNumber
            rollTextView.textSize = 20F
            rollTextView.layoutParams = textView.layoutParams
            rollTextView.gravity = Gravity.CENTER

            /** Set a text view for name of student*/
            val nameTextView = TextView(this)
            val name = student.firstName + " " + student.lastName
            nameTextView.text = name
            nameTextView.textSize = 20F
            nameTextView.layoutParams = textView.layoutParams
            nameTextView.background =
                ContextCompat.getDrawable(this, R.drawable.table_cell_background)
            nameTextView.gravity = Gravity.CENTER

            /** Add roll and name textviews to the the current row */
            tableRow.addView(rollTextView)
            tableRow.addView(nameTextView)
            tableRow.background =
                ContextCompat.getDrawable(this, R.drawable.table_cell_background)

            /** When user clicks on row, it turns green indicated the row is selcted,
             * if the user again clicks on it, it again turns into original color
             */
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

    /** This function makes a list of students
     * who were selected i.e. green in UI*/
    private fun getSelectedStudentsFromUI(students: List<Student>) {
        /** Check if last round checkbox is checked in UI */
        isLastRound = cb_declare_results.isChecked
        var index = 0
        val title = "Results declared"

        for (tableRowView in table_declare_results.children) {
            /** header row */
            if (index == 0) {
                index += 1
                continue
            }
            val tableRow = tableRowView as TableRow
            val rollNumberTextView = tableRow.getChildAt(0) as TextView

            /** Get original index of student from the hashmap */
            val studentIndex: Int = studentIndexHashmap[rollNumberTextView.text.toString()]!!

            /** -1 is done because first row is header row */
            val student = students[studentIndex]
            /** If row is green, then selected, else not */
            if (tableRow.background.constantState ==
                ContextCompat.getDrawable(
                    this,
                    R.drawable.table_cell_background_green
                )?.constantState
            ) {
                selectedStudentsIds.add(student.id)
                /** message to be shown to students
                 * according to whether it is last round or not
                 */
                val message = if (isLastRound)
                    "Hurray !!. You have been placed at ${company.name}."
                else
                    "Congratulations ${student.firstName} on clearing ${company.name}'s ${round.name} round"

                /** Notification to selected students **/
                SendNotificationToEligibleStudentsAsyncTask(title,message, student.fcmToken).execute()
            } else {
                notSelectedStudentsIds.add(student.id)
                val message = "You have not cleared the ${company.name}'s ${round.name} round"
                /** Notification to not selected students */
                SendNotificationToEligibleStudentsAsyncTask(title,message, student.fcmToken).execute()
            }
            index += 1
        }

        /** Update the company database by creating a hashmap*/
        val updatedRound = round.copy()
        updatedRound.selectedStudents = ArrayList(selectedStudentsIds)
        updatedRound.isOver = 1

        val updatedRoundsList = ArrayList(company.roundsList)
        updatedRoundsList.removeLast()
        updatedRoundsList.add(updatedRound)

        val companyHashmap = HashMap<String, Any>()
        companyHashmap[Constants.ROUNDS_LIST] = updatedRoundsList

        if (isLastRound) {
            companyHashmap[Constants.ROUNDS_OVER] = 1
        }
        updateCompanyDatabase(companyHashmap)
    }

    /** Update company in database */
    private fun updateCompanyDatabase(companyHashmap: HashMap<String, Any>) {
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().updateCompanyInCollegeDatabase(
            companyHashmap,
            company.name,
            collegeCode,
            this
        )
    }

    /** company database update successful */
    fun companyDatabaseUpdatedSuccess() {

        /** Update the selected student's database */
        val companyLastRoundObject =
            CompanyNameAndLastRound(company.name, round.number, lastRoundCleared = 1)
        val previousCompanyLastRoundObject =
            CompanyNameAndLastRound(company.name, round.number, lastRoundCleared = 2)

        var placed = 0
        if (isLastRound) {
            placed = 1
        }

        /** if this is last round, then update the placed field in
         * database, otherwise just update rest fields */
        if (placed == 1) {
            FirestoreClass().updateCompanyStatusInStudentDatabase(
                index = 0,
                selectedStudentsIds,
                companyLastRoundObject,
                previousCompanyLastRoundObject,
                activity = this,
                selectedStudentsUpdated = false,
                updatePlacedField = true,
                placedCompanyName = company.name
            )
        } else {
            FirestoreClass().updateCompanyStatusInStudentDatabase(
                index = 0,
                selectedStudentsIds,
                companyLastRoundObject,
                previousCompanyLastRoundObject,
                activity = this,
                selectedStudentsUpdated = false,
                updatePlacedField = false
            )
        }
    }

    /** selected students' database updated successfully */
    fun selectedStudentsDatabaseUpdatedSuccess() {

        /** Update the not selected student's database */
        val companyLastRoundObject =
            CompanyNameAndLastRound(company.name, round.number, lastRoundCleared = 0)
        val previousCompanyLastRoundObject =
            CompanyNameAndLastRound(company.name, round.number, lastRoundCleared = 2)

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

    /** not selected students' database updated successfully */
    fun notSelectedStudentsDatabaseUpdatedSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this,
            "Results updated. Please refresh the page to view changes",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }
}