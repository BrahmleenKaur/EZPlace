package com.example.ezplace.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.CompanyNameAndLastRound
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_new_company_details.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NewCompanyDetailsActivity : BaseActivity() {

    lateinit var collegeCode: String
    private val myCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.ezplace.R.layout.activity_new_company_details)

        setupActionBar(toolbar_new_company)

        /** Initialize college Code passed from previous activity i.e. Main activity */
        if (intent.hasExtra(Constants.COLLEGE_CODE))
            collegeCode = intent.getStringExtra(Constants.COLLEGE_CODE)!!

        addBranchesCheckboxesInLayout()

        val date =
            OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar.set(Calendar.YEAR, year)
                myCalendar.set(Calendar.MONTH, monthOfYear)
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateLabel()
            }

        et_deadline_to_apply.setOnClickListener {
            var myDatePicker = DatePickerDialog(
                this@NewCompanyDetailsActivity, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            )
            myDatePicker.datePicker.minDate = (System.currentTimeMillis())
            myDatePicker.show()
        }

        btn_submit_new_company.setOnClickListener {
            /**Call a function to add new company in the database */
            submitNewCompanyDetails()
        }
    }

    private fun updateLabel() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        et_deadline_to_apply.setText(sdf.format(myCalendar.time))
    }

    /** Shows the list of branches in the layout */
    private fun addBranchesCheckboxesInLayout() {
        val checkboxLinearLayout = ll_check_boxes

        for (i in Constants.ALL_BRANCHES.indices) {
            val checkBox = CheckBox(this)
            checkBox.text = Constants.ALL_BRANCHES[i]
            checkboxLinearLayout.addView(checkBox)
        }
    }

    private fun submitNewCompanyDetails() {
        /**Here we get the text from editText and trim the space */
        val companyName: String = et_new_company_name.text.toString().trim { it <= ' ' }
        val cgpaCutOffString: String = et_cgpa_cut_off.text.toString().trim { it <= ' ' }
        val ctcDetails = et_ctc_details.text.toString().trim { it <= ' ' }
        val jobProfile = et_job_profile.text.toString().trim { it <= ' ' }
        val companyLocation: String = et_location_new_company.text.toString().trim { it <= ' ' }
        val deadlineToApply: String = et_deadline_to_apply.text.toString().trim { it <= ' ' }
        val backLogsAllowed: Int = if (rb_backlogs_allowed.isSelected) 1 else 0

        var branchesAllowed: ArrayList<String> = ArrayList<String>()

        for (i in 0 until ll_check_boxes.childCount) {
            val checkBox: CheckBox = ll_check_boxes.getChildAt(i) as CheckBox
            if (checkBox.isChecked) {
                Log.i("branch", checkBox.text.toString())
                branchesAllowed.add(checkBox.text.toString())
            }
        }

        if (validateForm(
                companyName,
                cgpaCutOffString,
                branchesAllowed,
                ctcDetails,
                jobProfile,
                companyLocation,
                deadlineToApply
            )
        ) {
            val cgpaCutOff = cgpaCutOffString.toDouble()
            var deadlineLong : Long =0
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy")
                val date = sdf.parse(deadlineToApply)
                deadlineLong = date.time
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            var company = Company(
                companyName, cgpaCutOff,
                backLogsAllowed, branchesAllowed, ctcDetails,
                companyLocation, deadlineLong, jobProfile,
                ArrayList(), 0
            )

            /** Update company details in database */
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().addCompanyInCollege(company, collegeCode, this)
        }
    }

    fun companyRegisteredSuccess(company: Company) {
        hideProgressDialog()
        Toast.makeText(
            this, getString(R.string.company_is_added),
            Toast.LENGTH_LONG
        ).show()

        /**Get eligible students list according to company constraints */
        FirestoreClass().getEligibleStudents(company, collegeCode, this)
    }

    /** Successfully fetched list of eligible students according to company constraints */
    fun getEligibleStudentsSuccess(eligibleStudents: ArrayList<Student>, companyName: String) {
        hideProgressDialog()

        var eligibleStudentsIds: ArrayList<String> = ArrayList()

        /** Notify all these eligible students regarding new company
        This will be done in background using Async tasks*/
        for (student in eligibleStudents) {
            val token = student.fcmToken
            val id = student.id
            eligibleStudentsIds.add(id)
            SendNotificationToEligibleStudentsAsyncTask(companyName, token).execute()
        }

        /** Update the eligible student's database */
        showProgressDialog(getString(R.string.please_wait))
        val companyLastRoundObject = CompanyNameAndLastRound(companyName, 1)
        FirestoreClass().updateCompanyInStudentDatabase(
            eligibleStudentsIds,
            companyLastRoundObject,
            this
        )
    }

    fun updateCompanyInStudentDatabaseSuccess() {
        hideProgressDialog()
        finish()
    }

    /** A function to validate the details of a new company.*/
    private fun validateForm(
        companyName: String,
        cgpaCutOff: String,
        branchesAllowed: ArrayList<String>,
        ctcDetails: String,
        jobProfile: String,
        companyLocation: String,
        deadlineToApply: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(companyName) -> {
                showErrorSnackBar(getString(R.string.enter_company_name))
                false
            }
            TextUtils.isEmpty(cgpaCutOff) -> {
                showErrorSnackBar(getString(R.string.enter_cgpa_cut_off))
                false
            }
            rg_backlogs.checkedRadioButtonId == -1 -> {
                showErrorSnackBar(getString(R.string.enter_backlogs_details))
                false
            }
            branchesAllowed.size == 0 -> {
                showErrorSnackBar(getString(R.string.enter_at_least_one_branch))
                false
            }
            TextUtils.isEmpty(ctcDetails) -> {
                showErrorSnackBar(getString(R.string.enter_ctc_details))
                false
            }
            TextUtils.isEmpty(jobProfile) -> {
                showErrorSnackBar(getString(R.string.enter_job_profile))
                false
            }
            TextUtils.isEmpty(companyLocation) -> {
                showErrorSnackBar(getString(R.string.enter_company_location))
                false
            }
            TextUtils.isEmpty(deadlineToApply) -> {
                showErrorSnackBar(getString(R.string.enter_deadline_to_apply))
                false
            }
            else -> {
                true
            }
        }
    }


    /** Async Task to send notification to eligible students */
    private inner class SendNotificationToEligibleStudentsAsyncTask(
        val companyName: String,
        val token: String
    ) : AsyncTask<Any, Void, String>() {

        override fun doInBackground(vararg p0: Any?): String {
            var result: String

            /** REFERENCE
             * https://developer.android.com/reference/java/net/HttpURLConnection
             *
             */
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL) // Base Url
                connection = url.openConnection() as HttpURLConnection

                /**
                 * A URL connection can be used for input and/or output.  Set the DoOutput
                 * flag to true if you intend to use the URL connection for output,
                 * false if not.  The default is false.
                 */
                connection.doOutput = true
                connection.doInput = true

                /**
                 * Sets whether HTTP redirects should be automatically followed by this instance.
                 * The default value comes from followRedirects, which defaults to true.
                 */
                connection.instanceFollowRedirects = false

                /**
                 * Set the method for the URL request, one of:
                 *  POST
                 */
                connection.requestMethod = Constants.POST

                /**
                 * Sets the general request property. If a property with the key already
                 * exists, overwrite its value with the new value.
                 */
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                // The Server key can be found in
                // firebase console -> General settings -> cloud messaging ->Server key
                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION, "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                /**
                 * Some protocols do caching of documents.  Occasionally, it is important
                 * to be able to "tunnel through" and ignore the caches (e.g., the
                 * "reload" button in a browser).  If the UseCaches flag on a connection
                 * is true, the connection is allowed to use whatever caches it can.
                 *  If false, caches are to be ignored.
                 *  The default value comes from DefaultUseCaches, which defaults to
                 * true.
                 */
                connection.useCaches = false

                /**
                 * Creates a new data output stream to write data to the specified
                 * underlying output stream. The counter written is set to zero.
                 */
                val dataOutputStream = DataOutputStream(connection.outputStream)

                // CREATING NOTIFICATION DATA PAYLOAD
                // START
                // Create JSONObject Request
                val jsonRequest = JSONObject()

                // Create a data object
                val dataObject = JSONObject()
                // pass the title as per requirement
                dataObject.put(Constants.FCM_KEY_TITLE, "$companyName hiring")
                // Here you can pass the message as per requirement
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    getString(R.string.tap_to_view_more)
                )

                // Here add the data object and the user's token in the jsonRequest object.
                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)
                // END

                /**
                 * Writes out the string to the underlying output stream as a
                 * sequence of bytes. Each character in the string is written out, in
                 * sequence, by discarding its high eight bits. If no exception is
                 * thrown, the counter written is incremented by the
                 * length of s.
                 */
                dataOutputStream.writeBytes(jsonRequest.toString())
                dataOutputStream.flush() // Flushes this data output stream.
                dataOutputStream.close() // Closes this output stream and releases any system resources associated with the stream

                val httpResult: Int =
                    connection.responseCode // Gets the status code from an HTTP response message.

                if (httpResult == HttpURLConnection.HTTP_OK) {

                    /**
                     * Returns an input stream that reads from this open connection.
                     */
                    val inputStream = connection.inputStream

                    /**
                     * Creates a buffering character-input stream that uses a default-sized input buffer.
                     */
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    try {
                        /**
                         * Reads a line of text.  A line is considered to be terminated by any one
                         * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
                         * followed immediately by a linefeed.
                         */
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line + "\n")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            /**
                             * Closes this input stream and releases any system resources associated
                             * with the stream.
                             */
                            inputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                    result = stringBuilder.toString()
                } else {
                    /**
                     * Gets the HTTP response message, if any, returned along with the
                     * response code from a server.
                     */
                    result = connection.responseMessage
                }

            } catch (e: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (e: Exception) {
                result = "Error : " + e.message
            } finally {
                connection?.disconnect()
            }

            // You can notify with your result to onPostExecute.
            return result
        }

    }
}
