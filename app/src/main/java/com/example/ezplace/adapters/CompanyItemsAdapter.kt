package com.example.ezplace.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplace.R
import com.example.ezplace.models.Company
import kotlinx.android.synthetic.main.item_company.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class CompanyItemsAdapter(private val context: Context,
                            private var companiesList: ArrayList<Company>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates the item views which is designed in xml layout file
     *
     * creates a new
     * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_company,
                parent,
                false
            )
        )
    }

    /**
     * Binds each item in the ArrayList to a view
     *
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     *
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val company = companiesList[position]

        if (holder is MyViewHolder) {
            val lastRound : Int = company.roundsList.size
            var title = ""
            for(character in company.name){
                title += character.uppercaseChar()
            }
            title += " - Round $lastRound"

            holder.itemView.tv_item_company_name.text = title
            holder.itemView.tv_item_company_job_profile.text = company.jobProfile

            if(lastRound >=1){
                val dateString: String =
                    SimpleDateFormat("MM/dd/yyyy").format(Date(company.roundsList[lastRound-1].date))
                holder.itemView.tv_item_company_date.text = dateString
                holder.itemView.tv_item_company_time.text = company.roundsList[lastRound-1].time
            }
            else{
                holder.itemView.tv_item_company_date.text = "DATE"
                holder.itemView.tv_item_company_time.text = "TIME"
            }

            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, company)
                }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        // if else to avoid index out of bound
        return companiesList.size
    }

    /**
     * A function for OnClickListener where the Interface is the expected parameter..
     */
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * An interface for onclick items.
     */
    interface OnClickListener {
        fun onClick(position: Int, company : Company)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}