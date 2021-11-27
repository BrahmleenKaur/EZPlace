package com.example.ezplace.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.CompanyNameAndLastRound
import com.example.ezplace.models.Round
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_add_round.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AddRoundActivity : BaseActivity() {

    lateinit var company: Company
    lateinit var collegeCode: String
    lateinit var newRound: Round
    private val notEligibleStudentsIds = ArrayList<String>()

    /** Used for datePickerDialog */
    private val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_round)
        setupActionBar(toolbar_add_round)

        /** Get information passed through previous activity */
        val extras = intent.extras
        company = intent.getParcelableExtra<Company>(Constants.COMPANY_DETAIL)!!
        if (extras != null) {
            collegeCode = intent.extras?.getString(Constants.COLLEGE_CODE)!!
        }

        /** Initialise the date picker dialog */
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                et_date_add_round.setText(dateLongToString(myCalendar))
            }

        /** On clicking the date editText, date picker dialog should open */
        et_date_add_round.setOnClickListener {
            val myDatePicker = DatePickerDialog(
                this, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            myDatePicker.datePicker.minDate = (System.currentTimeMillis())
            myDatePicker.show()
        }

        btn_add_round.setOnClickListener {
            addRound()
        }
    }

    private fun addRound() {
        /**Here we get the text from editText and trim the space */
        val name: String = et_new_round_name.text.toString().trim { it <= ' ' }
        val dateString: String = et_date_add_round.text.toString().trim { it <= ' ' }
        val time = et_time_add_round.text.toString().trim { it <= ' ' }
        val venue = et_venue_add_round.text.toString().trim { it <= ' ' }

        /** validate the details **/
        if (validateRoundDetails(
                roundName = name, date = dateString, time = time, venue = venue
            )
        ) {
            /** Convert date from string to Long to store in database */
            var roundDate: Long = 0
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())
                val date = sdf.parse(dateString)
                roundDate = date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            /** Create a Round object and set it */
            val round = Round()
            round.number = company.roundsList.size + 1
            round.name = name
            round.date = roundDate
            round.time = time
            round.venue = venue

            /** If no student is shortlisted for this round, then mark the round as over */
            val isAnyStudentShortlisted = company.roundsList.last().selectedStudents.isNotEmpty()
            if (!isAnyStudentShortlisted) {
                round.isOver = 1
            }
            newRound = round

            /** Create a company hashmap to update the company in database*/
            val companyHashmap = HashMap<String, Any>()
            val updatedRoundsList = ArrayList(company.roundsList)
            updatedRoundsList.add(round)

            companyHashmap[Constants.ROUNDS_LIST] = updatedRoundsList
            /** If no student is shortlisted, mark the company's process as over and notify*/
            if (!isAnyStudentShortlisted) {
                companyHashmap[Constants.ROUNDS_OVER] = 1
                Toast.makeText(
                    this,
                    "No student is shortlisted for this round, so the rounds are over",
                    Toast.LENGTH_LONG
                ).show()
            }

            /** Update company in database */
            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().updateCompanyInCollegeDatabase(
                companyHashmap,
                company.name,
                collegeCode,
                this
            )
        }
    }

    /** Company updated successfully */
    fun updateCompanyDatabaseSuccess() {
        Toast.makeText(this, "New round added successfully", Toast.LENGTH_LONG).show()

        /** Eligible students for this new round are the selected students of previous round*/
        val eligibleStudentsIds: ArrayList<String> = company.roundsList.last().selectedStudents
        /** Get the students details from the database using their ids */
        FirestoreClass().getStudentsFromIds(eligibleStudentsIds, this)
    }

    /** Student details fetched successfully */
    fun getStudentsFromIdsSuccess(students: ArrayList<Student>) {
        val eligibleStudentsIds = ArrayList<String>()
        /** Notify all the eligible students using their fcm token**/
        for (student in students) {
            /** If the student is placed in some other company till now, then he/she will not move further
             * else the student will move further
             */
            if (student.placed == 0) {
                val title = "New round added"
                val message = "${company.name}'s ${newRound.name} round updates"
                eligibleStudentsIds.add(student.id)
                SendNotificationToEligibleStudentsAsyncTask(title,message, student.fcmToken).execute()
            } else {
                val title = "Round not cleared"
                val message =
                    "As you are already placed, you are not moving further with ${company.name}'s hiring rounds."
                notEligibleStudentsIds.add(student.id)
                SendNotificationToEligibleStudentsAsyncTask(title,message, student.fcmToken).execute()
            }
        }

        /** Update the eligible student's database , lastRoundCleared=2 means results pending, 1 means cleared*/
        val companyLastRoundObject =
            CompanyNameAndLastRound(company.name, newRound.number, lastRoundCleared = 2)
        val previousCompanyLastRoundObject =
            CompanyNameAndLastRound(company.name, newRound.number - 1, lastRoundCleared = 1)
        FirestoreClass().updateCompanyStatusInStudentDatabase(
            index = 0,
            studentsList = eligibleStudentsIds,
            companyLastRoundObject = companyLastRoundObject,
            previousCompanyLastRoundObject = previousCompanyLastRoundObject,
            activity = this,
            selectedStudentsUpdated = false,
            updatePlacedField = false
        )
    }

    /** Eligible students database updated successfully */
    fun updateEligibleStudentsDatabaseSuccess() {
        /** Update not eligible student's database , 0 means not cleared, 1 means cleared*/
        val companyLastRoundObject =
            CompanyNameAndLastRound(company.name, newRound.number, lastRoundCleared = 0)
        val previousCompanyLastRoundObject =
            CompanyNameAndLastRound(company.name, newRound.number - 1, lastRoundCleared = 1)

        FirestoreClass().updateCompanyStatusInStudentDatabase(
            index = 0,
            studentsList = notEligibleStudentsIds,
            companyLastRoundObject = companyLastRoundObject,
            previousCompanyLastRoundObject = previousCompanyLastRoundObject,
            activity = this,
            selectedStudentsUpdated = true,
            updatePlacedField = false
        )
    }

    /** Not eligible student's database updated successfully */
    fun updateNotEligibleStudentsDatabaseSuccess() {
        hideProgressDialog()
        finish()
    }

    /** A function to validate the details of a new round.*/
    private fun validateRoundDetails(
        roundName: String,
        date: String,
        time: String,
        venue: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(roundName) -> {
                showErrorSnackBar(getString(R.string.enter_round_name))
                false
            }
            TextUtils.isEmpty(date) -> {
                showErrorSnackBar(getString(R.string.enter_round_date))
                false
            }
            TextUtils.isEmpty(time) -> {
                showErrorSnackBar(getString(R.string.enter_round_time))
                false
            }
            TextUtils.isEmpty(venue) -> {
                showErrorSnackBar(getString(R.string.enter_round_venue))
                false
            }
            else -> {
                true
            }
        }
    }
}