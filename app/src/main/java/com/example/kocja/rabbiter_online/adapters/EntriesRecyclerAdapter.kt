package com.example.kocja.rabbiter_online.adapters

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.models.Entry
import kotlinx.android.synthetic.main.activity_add_entry.view.*
import kotlinx.android.synthetic.main.content_rabbit_linear_female.view.*


class EntriesRecyclerAdapter(private val c: Activity, private val allEntries: List<Entry>) : RecyclerView.Adapter<EntriesRecyclerAdapter.ViewHolder>() {
    private var listener: OnItemClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mainView = LayoutInflater.from(parent.context).inflate( if(viewType == 0) {
            R.layout.content_rabbit_linear_female
        }
        else{
            R.layout.content_rabbit_linear_male_group
        }, parent, false)

        return ViewHolder(mainView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(c)
                .load(allEntries[position].entryBitmap)
                .into(holder.itemView.rabbitView)
        holder.itemView.Gender.text = allEntries[position].chooseGender

        allEntries[position].birthDate?.let{
            holder.itemView.birthDate.text = it
        }
        allEntries[position].matedDate?.let{
            holder.itemView.matingDate?.text = it
        }


        holder.itemView.rabbitView.clipToOutline = true
        holder.itemView.Gender.setTextColor(when (allEntries[position].chooseGender) {
            "Female" -> Color.parseColor("#EC407A")
            "Male" -> Color.BLUE
            else -> Color.DKGRAY
        })

        holder.itemView.Title.text = if (allEntries[position].isMerged) {
            holder.itemView.mergedView.visibility = View.VISIBLE
            Glide.with(c)
                    .load(allEntries[position].mergedEntryBitmap)
                    .into(holder.itemView.mergedView)
            c.getString(R.string.mergedStrings, allEntries[position].entryName, allEntries[position].mergedEntryName)
        } else {
            allEntries[position].entryName
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
