package com.example.ezplace.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ezplace.R
import com.example.ezplace.activities.DeclareResultsActivity
import com.example.ezplace.activities.ViewResultsActivity
import com.example.ezplace.models.Round
import com.example.ezplace.utils.Constants
import kotlinx.android.synthetic.main.item_round.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

open class RoundItemsAdapter(
    private val context: Context,
    private var roundsList: ArrayList<Round>, private var lastClearedRound: Int = -1
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
                R.layout.item_round,
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
        val round = roundsList[position]

        if (holder is MyViewHolder) {

            val roundName: String = round.name
            val roundDate: Long = round.date
            val roundTime: String = round.time
            val roundVenue: String = round.venue

            holder.itemView.tv_item_round_name.text = roundName

            val myFormat = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            holder.itemView.tv_item_round_date.text = sdf.format(roundDate)
            holder.itemView.tv_item_round_time.text = roundTime
            holder.itemView.tv_item_round_venue.text = roundVenue

            val lastTwoRounds = ArrayList<Round>()
            val size = roundsList.size

            // lastClearedRound = -1 means it is tpo, else it is student whose min vale is 0
            if (lastClearedRound == -1) {
                if (round.isOver == 1) {
                    holder.itemView.tv_declare_results.text = Constants.VIEW_RESULTS
                    holder.itemView.tv_declare_results.setBackgroundColor(ContextCompat.getColor(context,R.color.green_dark))
                    holder.itemView.tv_item_round_status.text = Constants.OVER
                    holder.itemView.tv_item_round_status.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.dark_grey
                        )
                    )
                } else {
                    holder.itemView.tv_declare_results.text = Constants.DECLARE_RESULTS
                    holder.itemView.tv_item_round_status.text = Constants.PENDING
                    holder.itemView.tv_item_round_status.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.dark_blue
                        )
                    )
                    lastTwoRounds.add(roundsList[size-2])
                }
            } else {
                holder.itemView.cv_declare_results.visibility = View.GONE
                if (position < lastClearedRound)
                    holder.itemView.tv_item_round_status.text = Constants.CLEARED
                else {
                    if (round.isOver == 1) {
                        holder.itemView.tv_item_round_status.text = Constants.NOT_CLEARED
                        holder.itemView.tv_item_round_status.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.red
                            )
                        )
                    } else {
                        holder.itemView.tv_item_round_status.text = Constants.PENDING
                        holder.itemView.tv_item_round_status.setTextColor(
                            ContextCompat.getColor(
                                context,
                                R.color.dark_blue
                            )
                        )
                    }
                }
            }

            lastTwoRounds.add(round)
            holder.itemView.tag = lastTwoRounds
        }
    }

    /**
     * Gets the number of items in the list
     */
    override fun getItemCount(): Int {
        // if else to avoid index out of bound
        return roundsList.size
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
        fun onClick(position: Int, round: Round)
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     */
    private class MyViewHolder: RecyclerView.ViewHolder, View.OnClickListener{
        var btnResult =itemView.findViewById(R.id.cv_declare_results) as CardView
        var btnTextView =itemView.findViewById(R.id.tv_declare_results) as TextView
        var context: Context =itemView.context


        constructor(itemView: View) : super(itemView) {
            btnResult.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val lastTwoRounds= itemView.tag as ArrayList<Round>
            val round = lastTwoRounds.last()

            if(btnTextView.text == Constants.VIEW_RESULTS){
                val intent = Intent(context,ViewResultsActivity::class.java)
                intent.putExtra(Constants.ROUND,round)
                context.startActivity(intent)
            }
            else{
                val secondLastRound = lastTwoRounds[0]
                val intent = Intent(context,DeclareResultsActivity::class.java)
                intent.putExtra(Constants.ROUND,round)
                intent.putExtra(Constants.SECOND_LAST_ROUND,secondLastRound)
                context.startActivity(intent)
            }
        }
    }
}