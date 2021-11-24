package com.example.ezplace.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ezplace.R
import com.example.ezplace.adapters.CompanyItemsAdapter
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.CompanyNameAndLastRound
import com.example.ezplace.models.Student
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var resultLauncher: ActivityResultLauncher<Intent>

    /** Stores if the Floating Action Button is open or closed */
    private var isFABOpen: Boolean = false

    /** Stores if the user is student or TPO */
    private var isStudent: Boolean = true

    /**A variable to store details of current student*/
    private lateinit var mStudent: Student
    private lateinit var mTPO: TPO

    /** A SharedPreference object points to a file containing key-value pairs */
    /** Here it is used to store Firebase Cloud Messaging Token in the device */
    private lateinit var mSharedPreferences: SharedPreferences

    /**The college code to which the student or tpo belongs*/
    lateinit var collegeCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /** Using shared preference in private mode so that other apps cannot access it */
        mSharedPreferences =
            this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                refresh()
            }

        setUpNavigationView()
        handleIntent(intent)
    }

    private fun clickListenerForBottomNavigationView() {
        /** Set OnClick listeners for bottom navigation view options */
        bottomNavigationView.setOnItemSelectedListener {
            /**Create array of company names to fetch data from database*/
            var companiesList: ArrayList<String> = ArrayList()
            if (isStudent) {
                for ((companyName) in mStudent.companiesListAndLastRound) {
                    companiesList.add(companyName)
                }
            }
            when (it.itemId) {
                /** Shows list of Ongoing companies */
                R.id.bn_ongoing -> {
                    showProgressDialog(getString(R.string.please_wait))
                    if (isStudent) {
                        FirestoreClass().getSpecificCompaniesDetailsFromDatabase(
                            companyNames = companiesList,
                            collegeCode = collegeCode, this@MainActivity
                        )
                    } else {
                        FirestoreClass().getAllCompaniesDetailsFromDatabase(
                            collegeCode = collegeCode,
                            roundsOver = 0,
                            this@MainActivity
                        )
                    }
                }

                /** Shows list of previous companies whose process is over */
                R.id.bn_previous -> {
                    showProgressDialog(getString(R.string.please_wait))
                    if (isStudent) {
                        FirestoreClass().getSpecificCompaniesDetailsFromDatabase(
                            companyNames = companiesList,
                            collegeCode = collegeCode, this@MainActivity
                        )
                    } else {
                        FirestoreClass().getAllCompaniesDetailsFromDatabase(
                            collegeCode = collegeCode,
                            roundsOver = 1,
                            this@MainActivity
                        )
                    }
                }
            }
            true
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
        super.onNewIntent(intent)
    }

    private fun setUpNavigationView() {
        setSupportActionBar(toolbar_main_activity)
        /** 3 white lines Icon in the upper left corner for opening drawer layout */
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
        /**Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.*/
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun toggleDrawer() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.
            openDrawer(GravityCompat.START)
        }
    }

    /** Depending on the user is student or tpo, this function will load the screen*/
    private fun handleIntent(intent: Intent) {
        when {
            intent.hasExtra(Constants.STUDENT_DETAILS) -> {
                isStudent = true
                mStudent = intent.getParcelableExtra<Student>(Constants.STUDENT_DETAILS)!!
                collegeCode = mStudent.collegeCode
                clickListenerForBottomNavigationView()
                setForStudent()
            }
            intent.hasExtra(Constants.TPO_DETAILS) -> {
                isStudent = false
                mTPO = intent.getParcelableExtra<TPO>(Constants.TPO_DETAILS)!!
                collegeCode = mTPO.collegeCode
                clickListenerForBottomNavigationView()
                setForTPO(mTPO)
            }
        }
    }

    /** Setup UI for TPO */
    private fun setForTPO(tpo: TPO) {

        /**Disable "My Profile" option for TPO*/
        nav_view.menu[0].isVisible = false
        nav_view.menu[2].isVisible = false
        if (!mSharedPreferences.contains(Constants.PR_EMAIL))
            nav_view.menu[1].isVisible = false


        nav_view.setNavigationItemSelectedListener(this)
        val name = "Hi ${tpo.firstName}"
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_username).text = name


        /** Floating Action Button listener*/
        fab.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        fab_add_new_company.setOnClickListener {
            val intent: Intent = Intent(this, NewCompanyDetailsActivity::class.java)
            intent.putExtra(Constants.COLLEGE_CODE, collegeCode)
            resultLauncher.launch(intent)
            closeFABMenu()
        }

        fab_add_new_pr.setOnClickListener {
            val intent: Intent = Intent(this, AddPrActivity::class.java)
            intent.putExtra(Constants.COLLEGE_CODE, collegeCode)
            startActivity(intent)
            closeFABMenu()
        }

        fab_enable_or_disable_update_profile.setOnClickListener {
            val tvText = tv_enable_or_disable_update_profile.text.toString()
            if (tvText ==
                getString(R.string.disable_update_profile_button_fab)
            )
                showAlertDialog(this, getString(R.string.disable_update_profile_button))
            else
                showAlertDialog(this, getString(R.string.enable_update_profile_button))
        }

        tv_block_view.setOnClickListener {
            closeFABMenu()
        }

        loadCompaniesForTPO()

    }

    private fun loadCompaniesForTPO() {
        Log.i("tag main 2","done")
        val roundsOver = if (bottomNavigationView.menu[0].isChecked) 0 else 1
        /**Load TPO data to screen from database*/
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getAllCompaniesDetailsFromDatabase(
            collegeCode,
            roundsOver,
            this@MainActivity
        )
    }

    /** Setup UI for Student */
    private fun setForStudent() {

        nav_view.menu[1].isVisible = false

        nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_username).text =
            "Hi ${mStudent.firstName}"

        /**Floating Action Button is only visible to TPO*/
        fab.visibility = View.GONE
        fab_add_new_company.visibility = View.GONE
        fab_add_new_pr.visibility = View.GONE
        fab_enable_or_disable_update_profile.visibility = View.GONE

        if(mStudent.placed ==1){
            tv_placed_company_name.visibility = View.VISIBLE
            val placedMessage = " You have been placed at ${mStudent.placedCompanyName}."
            tv_placed_company_name.text = placedMessage
        }

        loadCompaniesForStudent()
    }

    private fun updateStudentDetails() {
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().loadStudentData(this)
    }

    fun updateStudentDetailsSuccess(student: Student) {
        mStudent = student
        hideProgressDialog()

        loadCompaniesForStudent()
    }

    private fun loadCompaniesForStudent() {

        /** To load student's data to screen */
        // Create array of companyNames to fetch data from database
        var companiesList: ArrayList<String> = ArrayList()

        for ((companyName, lastRound) in mStudent.companiesListAndLastRound) {
            companiesList.add(companyName)
        }
        showProgressDialog(getString(R.string.please_wait))

        FirestoreClass().getSpecificCompaniesDetailsFromDatabase(
            companiesList,
            collegeCode,
            this@MainActivity
        )
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        val addItem: MenuItem = menu.findItem(R.id.add)
        addItem.isVisible = false

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.placement_records -> {
                val intent = Intent(this, PlacementsRecordsActivity::class.java)
                intent.putExtra(Constants.COLLEGE_CODE, collegeCode)
                startActivity(intent)
                return true
            }
            R.id.refresh -> {
                refresh()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refresh() {
        Log.i("tag main 1","done")
        if (isStudent) {
            updateStudentDetails()
        } else {
            loadCompaniesForTPO()
        }
    }


    private fun showFABMenu() {
        isFABOpen = true

        tv_block_view.visibility = View.VISIBLE
        fab.setImageResource(R.drawable.ic_wrong)
        fab.scaleType = ImageView.ScaleType.FIT_XY

        ll_add_new_company.animate().translationY(-resources.getDimension(R.dimen.standard_55))
        ll_add_new_pr.animate().translationY(-resources.getDimension(R.dimen.standard_105))
        ll_enable_or_disable_update_profile.animate()
            .translationY(-resources.getDimension(R.dimen.standard_155))

        tv_add_new_company.visibility = View.VISIBLE
        tv_add_new_pr.visibility = View.VISIBLE
        tv_enable_or_disable_update_profile.visibility = View.VISIBLE
    }

    private fun closeFABMenu() {
        isFABOpen = false

        tv_block_view.visibility = View.GONE
        fab.setImageResource(R.drawable.ic_add)
        fab.scaleType = ImageView.ScaleType.FIT_XY

        ll_add_new_company.animate().translationY(0F)
        ll_add_new_pr.animate().translationY(0F)
        ll_enable_or_disable_update_profile.animate().translationY(0F)

        tv_add_new_company.visibility = View.GONE
        tv_add_new_pr.visibility = View.GONE
        tv_enable_or_disable_update_profile.visibility = View.GONE
    }

    /** Shows items in recycler view */
    fun populateRecyclerView(companiesList: ArrayList<Company>) {
        Log.i("tag main 3","done")
        hideProgressDialog()

        val updatedCompaniesList = ArrayList<Company>()
        if (isStudent) {
            for (company in companiesList) {
                val clearedCompaniesObject =
                    CompanyNameAndLastRound(company.name, company.roundsList.last().number, 1)
                val pendingResultsCompaniesObject =
                    CompanyNameAndLastRound(company.name, company.roundsList.last().number, 2)
                val isClearedCompany = mStudent.companiesListAndLastRound.contains(
                    clearedCompaniesObject
                )
                val isPendingResultsCompany = mStudent.companiesListAndLastRound.contains(
                    pendingResultsCompaniesObject
                )
                val roundsOver = company.roundsOver == 1
                if (bottomNavigationView.menu[0].isChecked) {

                    if (!roundsOver && (isClearedCompany || isPendingResultsCompany)) {
                        updatedCompaniesList.add(company)
                    }
                } else {
                    if (roundsOver || !(isClearedCompany || isPendingResultsCompany)) {
                        updatedCompaniesList.add(company)
                    }
                }
            }
        } else {
            for (company in companiesList) {
                val roundsOver = company.roundsOver == 1
                if (bottomNavigationView.menu[0].isChecked) {
                    if (!roundsOver) updatedCompaniesList.add(company)
                }
                else{
                    if (roundsOver) updatedCompaniesList.add(company)
                }
            }
        }

        if (updatedCompaniesList.size > 0) {

            /** Setting up recycler view */
            rv_companies_list.visibility = View.VISIBLE
            tv_no_companies_available.visibility = View.GONE

            rv_companies_list.layoutManager = LinearLayoutManager(this@MainActivity)
            rv_companies_list.setHasFixedSize(true)

            val adapter = CompanyItemsAdapter(this, updatedCompaniesList)
            rv_companies_list.adapter = adapter

            /** On click listener for each item of recycler view */
            adapter.setOnClickListener(object : CompanyItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Company) {
                    val intent = Intent(this@MainActivity, RoundDetailsActivity::class.java)
                    if (isStudent)
                        intent.putExtra(Constants.STUDENT_DETAILS, mStudent)
                    intent.putExtra(Constants.COMPANY_DETAIL, model)
                    intent.putExtra(Constants.COLLEGE_CODE, collegeCode)
                    resultLauncher.launch(intent)
                }
            })
        } else {
            rv_companies_list.visibility = View.GONE
            tv_no_companies_available.visibility = View.VISIBLE
        }

        /** Check changes in Firebase Cloud Messaging token */
        if (isStudent) {
            checkFCMToken()
        }
    }

    private fun checkFCMToken() {

        /** Variable is used get the value either token is updated in the database or not.*/
        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        /**Here if the token is already updated than we don't need to update it every time. */
        /** If the token is not updated, get a new token and update it in database*/
        if (!tokenUpdated) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) { token ->
                    updateFcmTokenInDatabase(token)
                }
        }
    }

    /** Update Firebase Cloud Messaging Token in Database */
    private fun updateFcmTokenInDatabase(token: String) {
        val studentHashMap = HashMap<String, Any>()
        studentHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().updateStudentProfileData(this, studentHashMap)
    }

    /** Successfully Updated Firebase Cloud Messaging Token */
    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
    }

    /** Listeners for each option inside drawer layout */
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {

            R.id.nav_my_profile -> {
                menuItem.isCheckable = false
                /** Takes user to UpdateProfile activity */
                startActivity(Intent(this, UpdateProfileActivity::class.java))
            }

            R.id.switch_to_pr_account -> {
                menuItem.isCheckable = false
                if (mSharedPreferences.contains(Constants.PR_EMAIL)) {
                    val email = mSharedPreferences.getString(Constants.PR_EMAIL, "default")
                    val password = mSharedPreferences.getString(Constants.PR_PASSWORD, "default")

                    val intent = Intent(this, SignInActivity::class.java)
                    intent.putExtra(Constants.PR_EMAIL, email)
                    intent.putExtra(Constants.PR_PASSWORD, password)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, SignInActivity::class.java)
                    intent.putExtra(Constants.IS_PR, true)
                    startActivity(intent)
                }
            }

            R.id.switch_to_student_account -> {
                menuItem.isCheckable = false
                if (mSharedPreferences.contains(Constants.STUDENT_EMAIL)) {
                    val email = mSharedPreferences.getString(Constants.STUDENT_EMAIL, "default")
                    val password =
                        mSharedPreferences.getString(Constants.STUDENT_PASSWORD, "default")

                    val intent = Intent(this, SignInActivity::class.java)
                    intent.putExtra(Constants.STUDENT_EMAIL, email)
                    intent.putExtra(Constants.STUDENT_PASSWORD, password)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, IntroActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.nav_sign_out -> {
                menuItem.isCheckable = false
                /**Here sign outs the user from firebase in this device.*/
                showAlertDialog(this, getString(R.string.sign_out_alert_text))
            }
        }

        /** Close the drawer*/
        drawer_layout.closeDrawer(GravityCompat.START)

        return true
    }

    /** clears the Firebase Cloud Messaging token stored in device */
    fun clearSharedPreferences() {
        if (isStudent) {
            mSharedPreferences.edit().remove(Constants.FCM_TOKEN_UPDATED).apply()
            mSharedPreferences.edit().remove(Constants.STUDENT_EMAIL).apply()

            if (mSharedPreferences.contains(Constants.PR_EMAIL)) {
                val email = mSharedPreferences.getString(Constants.PR_EMAIL, "default")
                val password = mSharedPreferences.getString(Constants.PR_PASSWORD, "default")

                val intent = Intent(this, SignInActivity::class.java)
                intent.putExtra(Constants.PR_EMAIL, email)
                intent.putExtra(Constants.PR_PASSWORD, password)
                startActivity(intent)
                finish()
            } else sendToIntroActivity()
        } else if (mSharedPreferences.contains(Constants.PR_EMAIL)) {
            mSharedPreferences.edit().remove(Constants.PR_EMAIL).apply()

            if (mSharedPreferences.contains(Constants.STUDENT_EMAIL)) {
                val email = mSharedPreferences.getString(Constants.STUDENT_EMAIL, "default")
                val password =
                    mSharedPreferences.getString(Constants.STUDENT_PASSWORD, "default")

                val intent = Intent(this, SignInActivity::class.java)
                intent.putExtra(Constants.STUDENT_EMAIL, email)
                intent.putExtra(Constants.STUDENT_PASSWORD, password)
                startActivity(intent)
                finish()
            } else sendToIntroActivity()
        } else sendToIntroActivity()
    }

    private fun sendToIntroActivity() {
        //SEnd the user to Intro activity after signing out
        val intent = Intent(this, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun updateCollegeSuccess() {
        hideProgressDialog()
    }
}