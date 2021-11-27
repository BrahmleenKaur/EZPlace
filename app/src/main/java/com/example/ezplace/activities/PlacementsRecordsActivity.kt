package com.example.ezplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ezplace.R
import com.example.ezplace.adapters.CompanyForPlacementRecordAdapter
import com.example.ezplace.adapters.CompanyItemsAdapter
import com.example.ezplace.firebase.FirestoreClass
import com.example.ezplace.models.Company
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.activity_placements_records.*
import kotlinx.android.synthetic.main.content_main.*

class PlacementsRecordsActivity : BaseActivity() {

    private var collegeCode =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placements_records)
        setupActionBar(toolbar_placements_records)

        /** get college code from intent */
        val extras : Bundle?= intent.extras
        collegeCode = extras!!.getString(Constants.COLLEGE_CODE,"college code")

        /** Fetch company details from database */
        showProgressDialog(getString(R.string.please_wait))
        FirestoreClass().getAllCompaniesDetailsFromDatabase(collegeCode, roundsOver = 1,this)
    }

    fun populateRecyclerView(companiesList : ArrayList<Company>){
        hideProgressDialog()

        if (companiesList.size > 0) {
            /** Setting up recycler view */
            rv_companies_list_placement_records.visibility = View.VISIBLE
            tv_no_companies_available_placement_records.visibility = View.GONE

            rv_companies_list_placement_records.layoutManager = LinearLayoutManager(this)
            rv_companies_list_placement_records.setHasFixedSize(true)

            val adapter = CompanyForPlacementRecordAdapter(this, companiesList)
            rv_companies_list_placement_records.adapter = adapter

            /** On click listener for each item of recycler view */
            adapter.setOnClickListener(object : CompanyForPlacementRecordAdapter.OnClickListener {
                override fun onClick(position: Int, company: Company) {
                    val intent = Intent(this@PlacementsRecordsActivity, PlacedStudentsActivity::class.java)
                    intent.putExtra(Constants.COMPANY_DETAIL, company)
                    startActivity(intent)
                }
            })
        } else {
            rv_companies_list_placement_records.visibility = View.GONE
            tv_no_companies_available_placement_records.visibility = View.VISIBLE
        }
    }
}