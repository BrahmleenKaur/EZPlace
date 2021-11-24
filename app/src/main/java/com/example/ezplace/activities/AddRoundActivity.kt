package com.example.ezplace.activities

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_declare_results.*
import kotlinx.android.synthetic.main.activity_new_company_details.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

class AddRoundActivity : BaseActivity() {

    lateinit var company : Company
    lateinit var collegeCode : String
    lateinit var newRound : Round
    private val notEligibleStudentsIds = ArrayList<String>()

    private val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_round)

        setupActionBar(toolbar_add_round)

        val extras = intent.extras
        company = intent.getParcelableExtra<Company>(Constants.COMPANY_DETAIL)!!
        if (extras != null) {
            collegeCode = intent.extras?.getString(Constants.COLLEGE_CODE)!!
        }

        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                et_date_add_round.setText(dateLongToString(myCalendar))
            }

        et_date_add_round.setOnClickListener {
            var myDatePicker = DatePickerDialog(
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

    private fun addRound(){
        /**Here we get the text from editText and trim the space */
        val name: String = et_new_round_name.text.toString().trim { it <= ' ' }
        val dateString: String = et_date_add_round.text.toString().trim { it <= ' ' }
        val time = et_time_add_round.text.toString().trim { it <= ' ' }
        val venue = et_venue_add_round.text.toString().trim { it <= ' ' }

        if (validateForm(
                name,dateString,time,venue
            )
        ) {
            var roundDate : Long =0
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                val date = sdf.parse(dateString)
                roundDate = date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            var round = Round()
            round.number = company.roundsList.size +1
            round.name=name
            round.date=roundDate
            round.time=time
            round.venue=venue
            val isAnyStudentShortlisted = company.roundsList.last().selectedStudents.isNotEmpty()
            if(!isAnyStudentShortlisted){
                round.isOver = 1
            }

            newRound=round

            val updatedRoundsList = ArrayList(company.roundsList)
            updatedRoundsList.add(round)

            val companyHashmap = HashMap<String,Any>()
            companyHashmap[Constants.ROUNDS_LIST] = updatedRoundsList
            if(!isAnyStudentShortlisted){
                companyHashmap[Constants.ROUNDS_OVER] =1
                Toast.makeText(this,"No student is shortlisted for this round, so the rounds are over",Toast.LENGTH_LONG).show()
            }

            showProgressDialog(getString(R.string.please_wait))
            FirestoreClass().updateCompanyInCollegeDatabase(companyHashmap,company.name,collegeCode,this)
        }
    }

    fun updateCompanyDatabaseSuccess(){
        Toast.makeText(this,"New round added successfully",Toast.LENGTH_LONG).show()

        var eligibleStudentsIds : ArrayList<String> =company.roundsList.last().selectedStudents
        FirestoreClass().getStudentsFromIds(eligibleStudentsIds,this)
    }

    fun getStudentsFromIdsSuccess(students: ArrayList<Student>){
        val eligibleStudentsIds = ArrayList<String>()
        for(student in students){
            if(student.placed==0){
                val message = "${company.name}'s ${newRound.name} round updates"
                eligibleStudentsIds.add(student.id)
                SendNotificationToEligibleStudentsAsyncTask(message, student.fcmToken).execute()
            }
            else{
                val message = "As you are already placed, you are not moving further with ${company.name}'s hiring rounds."
                notEligibleStudentsIds.add(student.id)
                SendNotificationToEligibleStudentsAsyncTask(message, student.fcmToken).execute()
            }
        }

        /** Update the eligible student's database */
        val companyLastRoundObject = CompanyNameAndLastRound(company.name, newRound.number,2)
        val previousCompanyLastRoundObject = CompanyNameAndLastRound(company.name, newRound.number -1,1)
        FirestoreClass().updateCompanyStatusInStudentDatabase(
            0,
            eligibleStudentsIds,
            companyLastRoundObject,
            previousCompanyLastRoundObject,
            this,
            selectedStudentsUpdated = false,
            updatePlacedField = false
        )
    }

    fun updateEligibleStudentsDatabaseSuccess(){
        /** Update not eligible student's database */
        val companyLastRoundObject = CompanyNameAndLastRound(company.name, newRound.number,0)
        val previousCompanyLastRoundObject = CompanyNameAndLastRound(company.name, newRound.number-1,1)
        FirestoreClass().updateCompanyStatusInStudentDatabase(
            0,
            notEligibleStudentsIds,
            companyLastRoundObject,
            previousCompanyLastRoundObject,
            this,
            selectedStudentsUpdated = true,
            updatePlacedField = false
        )
    }

    fun updateNotEligibleStudentsDatabaseSuccess(){
        hideProgressDialog()
        finish()
    }

    /** A function to validate the details of a new company.*/
    private fun validateForm(
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