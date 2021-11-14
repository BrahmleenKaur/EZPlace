package com.example.ezplace.activities

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.core.view.get
import com.example.ezplace.R
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Student
import com.example.ezplace.models.TPO
import com.example.ezplace.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.HashMap

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var isFABOpen = false
    private var isStudent = true

    private lateinit var mTPO: TPO
    private lateinit var mStudent: Student
    private lateinit var mSharedPreferences: SharedPreferences
    private lateinit var collegeCode : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        handleIntent(intent)

        if(isStudent){
            mSharedPreferences =
                this.getSharedPreferences(Constants.EZ_PLACE_PREFERENCES, Context.MODE_PRIVATE)

            val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

            if (tokenUpdated) {
                showProgressDialog(getString(R.string.please_wait))
                FirestoreClass().loadStudentData(this)
            } else {
                FirebaseMessaging.getInstance().token
                    .addOnSuccessListener(this@MainActivity) { token ->
                        updateFcmTokenInDatabase(token)
                    }
            }
        }

        rb_ongoing.setOnClickListener {
            tv_no_companies_available.setText("Ongoing")
        }
        rb_previous.setOnClickListener {
            tv_no_companies_available.text = "Previous"
        }
        rb_upcoming.setOnClickListener {
            tv_no_companies_available.text = "Upcoming"
        }
    }

    private fun showFABMenu() {
        isFABOpen = true

        tv_block_view.visibility = View.VISIBLE

        fab.setImageResource(R.drawable.ic_wrong)
        fab.scaleType = ImageView.ScaleType.FIT_XY

        ll_add_new_company.animate().translationY(-resources.getDimension(R.dimen.standard_55))

        tv_add_new_company.visibility = View.VISIBLE
    }

    private fun closeFABMenu() {
        isFABOpen = false

        tv_block_view.visibility = View.GONE

        fab.setImageResource(R.drawable.ic_fab)
        fab.scaleType = ImageView.ScaleType.FIT_XY

        ll_add_new_company.animate().translationY(0F)

        tv_add_new_company.visibility = View.GONE
    }

    private fun toggleDrawer() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when {
            intent.hasExtra(Constants.STUDENT_DETAILS) -> {
                isStudent = true
                mStudent = intent.getParcelableExtra<Student>(Constants.STUDENT_DETAILS)!!
                setForStudent()
            }
            intent.hasExtra(Constants.IS_STUDENT) -> {
                isStudent = true
                mStudent = intent.getParcelableExtra<Student>(Constants.STUDENT_DETAILS)!!
                collegeCode = mStudent.collegeCode
                setForStudent()
            }
            intent.hasExtra(Constants.TPO_DETAILS) -> {
                isStudent = false
                mTPO = intent.getParcelableExtra<TPO>(Constants.TPO_DETAILS)!!
                collegeCode = mTPO.collegeCode
                setForTPO()
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setForTPO() {
        Log.i("here","tpo")
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }
        nav_view.menu[0].isVisible = false
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_username).text =
            "Hi ${mTPO.firstName}"

        //TODO get list of companies

        //Fab listener
        fab.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        fab_add_new_company.setOnClickListener {
            val intent : Intent = Intent(this, NewCompanyDetailsActivity::class.java)
            intent.putExtra(Constants.COLLEGE_CODE,collegeCode)
            startActivity(intent)
            closeFABMenu()
        }

        tv_block_view.setOnClickListener {
            closeFABMenu()
        }

    }

    private fun setForStudent() {
        Log.i("setForStudent", "setForStudent")
        setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
            toggleDrawer()
        }

        //TODO
//        showProgressDialog(resources.getString(R.string.please_wait))
//        updateNavigationUserDetails(mStudent)

        // Assign the NavigationView.OnNavigationItemSelectedListener to navigation view.
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.tv_username).text =
            "Hi ${mStudent.firstName}"

        fab.visibility = View.GONE
        fab_add_new_company.visibility = View.GONE
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {
                startActivity(Intent(this, UpdateProfileActivity::class.java))
                menuItem.isChecked=false
            }
            R.id.nav_sign_out -> {
                // Here sign outs the user from firebase in this device.
                showAlertDialog(this@MainActivity, getString(R.string.sign_out_alert_text))

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().loadStudentData(this)
    }

    private fun updateFcmTokenInDatabase(token: String) {
        val studentHashMap = HashMap<String, Any>()
        studentHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().updateStudentProfileData(this, studentHashMap)
    }

    fun clearSharedPreferences(){
        if(isStudent)  mSharedPreferences.edit().clear().apply()
    }

    fun loadStudentDataSuccess(student : Student){
        hideProgressDialog()
    }

}