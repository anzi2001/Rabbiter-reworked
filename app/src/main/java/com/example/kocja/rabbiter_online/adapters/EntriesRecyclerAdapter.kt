package com.example.kocja.rabbiter_online.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.api.load

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.models.Entry
import kotlinx.android.synthetic.main.content_rabbit_linear_female.view.*


class EntriesRecyclerAdapter(private val c: Context, private val allEntries: List<Entry>) : RecyclerView.Adapter<EntriesRecyclerAdapter.ViewHolder>() {
    private var clickListener : View.OnClickListener? = null
    private var longClickListener : View.OnLongClickListener? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

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
        mainView.setOnClickListener(clickListener)
        mainView.setOnLongClickListener(longClickListener)

        return ViewHolder(mainView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.rabbitView.load(allEntries[position].entryPhotoURL){
            crossfade(500)
        }

        holder.itemView.Gender.text = allEntries[position].chooseGender

        allEntries[position].birthDate?.let{
            holder.itemView.birthDate.text = it
        }

        if(holder.itemView.matingDate != null){
            allEntries[position].matedDate?.let{
                holder.itemView.matingDate?.text = it
            }
        }



        holder.itemView.rabbitView.clipToOutline = true
        holder.itemView.Gender.setTextColor(when (allEntries[position].chooseGender) {
            "Female" -> Color.parseColor("#EC407A")
            "Male" -> Color.BLUE
            else -> Color.DKGRAY
        })

        holder.itemView.Title.text = if (allEntries[position].isMerged) {
            holder.itemView.mergedView.visibility = View.VISIBLE
            holder.itemView.mergedView.load(allEntries[position].mergedEntryPhotoURL)

            c.getString(R.string.mergedStrings, allEntries[position].entryName, allEntries[position].mergedEntryName)
        } else {
            holder.itemView.mergedView.visibility = View.GONE
            allEntries[position].entryName
        }
    }


    override fun getItemCount(): Int {
        return allEntries.size
    }

    fun setClickListeners(itemClickListener: View.OnClickListener, longItemClickListener : View.OnLongClickListener) {
        clickListener = itemClickListener
        longClickListener = longItemClickListener
    }
}
