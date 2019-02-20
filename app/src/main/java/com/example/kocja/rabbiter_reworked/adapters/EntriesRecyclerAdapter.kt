package com.example.kocja.rabbiter_reworked.adapters

import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.databases.Entry

import java.text.SimpleDateFormat
import java.util.Locale


class EntriesRecyclerAdapter(private val c: Activity, private val allEntries: List<Entry>) : RecyclerView.Adapter<EntriesRecyclerAdapter.ViewHolder>() {
    private var listener: OnItemClickListener? = null
    private val format = SimpleDateFormat("dd/MM/yyyy", Locale.GERMAN)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val entryImage: ImageView = itemView.findViewById(R.id.rabbitView)
        val titleName: TextView = itemView.findViewById(R.id.Title)
        val genderView: TextView = itemView.findViewById(R.id.Gender)
        val birthDateView: TextView = itemView.findViewById(R.id.BirthDate)
        val matingDateView: TextView? = itemView.findViewById(R.id.MatingDate)

        var mergedImage: ImageView = itemView.findViewById(R.id.mergedView)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            view.tag = allEntries[adapterPosition].entryID
            listener!!.onItemClick(view, adapterPosition)

        }

        override fun onLongClick(view: View): Boolean {
            view.tag = allEntries[adapterPosition].entryID
            listener!!.onLongItemClick(view, adapterPosition)
            return true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(allEntries[position].chooseGender){
            "Female"->  0
            else-> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntriesRecyclerAdapter.ViewHolder {
        val mainView = LayoutInflater.from(parent.context).inflate( if(viewType == 0) {
                R.layout.content_rabbit_linear_female
            }
            else{
                R.layout.content_rabbit_linear_male_group
            }, parent, false)

        return ViewHolder(mainView)
    }

    override fun onBindViewHolder(holder: EntriesRecyclerAdapter.ViewHolder, position: Int) {

        Glide.with(c)
                .load(allEntries[position].entryPhLoc)
                .into(holder.entryImage)
        holder.genderView.text = allEntries[position].chooseGender
        if (allEntries[position].birthDate != null) {
            holder.birthDateView.text = format.format(allEntries[position].birthDate)
        }
        if (allEntries[position].matedDate != null) {
            holder.matingDateView?.text = format.format(allEntries[position].matedDate)
        }


        holder.entryImage.clipToOutline = true

        when (allEntries[position].chooseGender) {
            "Female" -> holder.genderView.setTextColor(Color.parseColor("#EC407A"))
            "Male" -> holder.genderView.setTextColor(Color.BLUE)
            else -> holder.genderView.setTextColor(Color.DKGRAY)
        }

        if (allEntries[position].isMerged) {
            holder.mergedImage.visibility = View.VISIBLE
            Glide.with(c)
                    .load(allEntries[position].mergedEntryPhLoc)
                    .into(holder.mergedImage)
            holder.titleName.text = c.getString(R.string.mergedStrings, allEntries[position].entryName, allEntries[position].mergedEntryName)
        } else {
            holder.titleName.text = allEntries[position].entryName
        }

    }

    override fun getItemCount(): Int {
        return allEntries.size
    }

    fun setLongClickListener(itemClickListener: OnItemClickListener) {
        listener = itemClickListener
    }

    interface OnItemClickListener {
        fun onLongItemClick(view: View, position: Int)
        fun onItemClick(view: View, position: Int)
    }
}
