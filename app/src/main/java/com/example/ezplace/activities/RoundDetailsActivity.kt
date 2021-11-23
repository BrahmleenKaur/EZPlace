package com.example.ezplace.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ezplace.R
import com.example.ezplace.adapters.RoundItemsAdapter
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.models.Round
import com.example.ezplace.models.Student
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_round_details.*
import kotlinx.android.synthetic.main.content_main.*

class RoundDetailsActivity : BaseActivity() {

    private lateinit var company : Company
    private lateinit var collegeCode : String
    private lateinit var mStudent : Student
    private var isStudent = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_round_details)

        company = intent.getParcelableExtra<Company>(Constants.COMPANY_DETAIL)!!
        collegeCode = intent.extras?.getString(Constants.COLLEGE_CODE)!!

        toolbar_round_details.title = company.name + "$'s rounds"
        setupActionBar(toolbar_round_details)

        onCreateOptionsMenu(toolbar_round_details.menu)
        handleIntent(intent)

        fab_about_company.setOnClickListener {
            val intent = Intent(this, AboutCompanyActivity::class.java)
            intent.putExtra(Constants.COMPANY_DETAIL, company)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        val addItem: MenuItem = menu.findItem(R.id.add)
        val recordsItem: MenuItem = menu.findItem(R.id.placement_records)
        recordsItem.isVisible = false

        if(isStudent){
            addItem.isVisible = false
        }
        else{
            val roundsList =company.roundsList
            if(roundsList.isNotEmpty()){
                val lastRound=roundsList.last()
                if(lastRound.isOver == 0){
                    disableAddMenuItem()
                }
                else{
                    enableAddMenuItem()
                }
            }
        }
        return true
    }

    private fun disableAddMenuItem(){
        val menu : Menu = toolbar_round_details.menu
        val addItem: MenuItem = menu.findItem(R.id.add)
        addItem.isEnabled = false
        addItem.icon=ContextCompat.getDrawable(this, R.drawable.ic_add_grey)
    }
    private fun enableAddMenuItem(){
        val menu : Menu = toolbar_round_details.menu
        val addItem: MenuItem = menu.findItem(R.id.add)
        addItem.isEnabled = true
        addItem.icon=ContextCompat.getDrawable(this, R.drawable.ic_add)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add ->{
                val intent = Intent(this, AddRoundActivity::class.java)
                intent.putExtra(Constants.COMPANY_DETAIL, company)
                intent.putExtra(Constants.COLLEGE_CODE, collegeCode)
                startActivity(intent)
                refresh()
            }
            R.id.refresh ->{
                refresh()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refresh(){
        showProgressDialog(getString(R.string.please_wait))
        if(isStudent) FirestoreClass().loadStudentData(this)
        else FirestoreClass().loadCompany(company.name,collegeCode,this)
    }

    fun loadStudentDataSuccess(student: Student){
        mStudent=student
        FirestoreClass().loadCompany(company.name,collegeCode,this)
    }

    fun loadCompanySuccess(updatedCompany: Company){
        hideProgressDialog()

        company=updatedCompany
        if(isStudent) setForStudent()
        else setForTpo()
    }

    /** Depending on the user is student or tpo, this function will load the screen*/
    private fun handleIntent(intent: Intent) {
        when {
            intent.hasExtra(Constants.STUDENT_DETAILS) -> {
                isStudent = true
                mStudent = intent.getParcelableExtra<Student>(Constants.STUDENT_DETAILS)!!
                setForStudent()
            }
            else ->{
                isStudent=false
                setForTpo()
            }
        }
    }

    private fun setForTpo() {
        val roundsList : ArrayList<Round> = company.roundsList

        if (roundsList.size > 0) {
            /** Setting up recycler view */
            rv_rounds_list.visibility = View.VISIBLE
            tv_no_rounds_added.visibility = View.GONE

            rv_rounds_list.layoutManager = LinearLayoutManager(this)
            rv_rounds_list.setHasFixedSize(true)

            val adapter = RoundItemsAdapter(this, roundsList,-1,company = company)
            rv_rounds_list.adapter = adapter

            if(roundsList.last().isOver == 0){
                disableAddMenuItem()
            }
            else{
                enableAddMenuItem()
            }
        } else {
            rv_rounds_list.visibility = View.GONE
            tv_no_rounds_added.visibility = View.VISIBLE
        }
    }

    private fun setForStudent() {
        val roundsList : ArrayList<Round> = company.roundsList
        var studentRoundsList : ArrayList<Round> = ArrayList()
        var lastRoundForCompany =0

        for((companyName,lastRound) in mStudent.companiesListAndLastRound){
            if(companyName == company.name){
                lastRoundForCompany=lastRound
                break
            }
        }

        for(index in 0 until Math.min(lastRoundForCompany,roundsList.size)){
            studentRoundsList.add(roundsList[index])
        }

        if (studentRoundsList.size > 0) {

            /** Setting up recycler view */
            rv_rounds_list.visibility = View.VISIBLE
            tv_no_rounds_added.visibility = View.GONE

            rv_rounds_list.layoutManager = LinearLayoutManager(this)
            rv_rounds_list.setHasFixedSize(true)

            val adapter = RoundItemsAdapter(this, roundsList,lastRoundForCompany-1,company)
            rv_rounds_list.adapter = adapter

            /** On click listener for each item of recycler view */
//            adapter.setOnClickListener(object : CompanyItemsAdapter.OnClickListener {
//                override fun onClick(position: Int, model: Company) {
//                    val intent = Intent(this@MainActivity, RoundDetailsActivity::class.java)
//                    if (isStudent)
//                        intent.putExtra(Constants.STUDENT_DETAILS, mStudent)
//                    intent.putExtra(Constants.COMPANY_DETAIL, model)
//                    intent.putExtra(Constants.COLLEGE_CODE, collegeCode)
//                    startActivity(intent)
//                }
//            })
        } else {
            rv_rounds_list.visibility = View.GONE
            tv_no_rounds_added.visibility = View.VISIBLE
        }
    }

}