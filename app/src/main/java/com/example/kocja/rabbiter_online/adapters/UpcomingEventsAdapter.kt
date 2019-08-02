package com.example.kocja.rabbiter_online.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.kocja.rabbiter_online.R


import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.upcoming_event_layout.view.*


class UpcomingEventsAdapter(private val eventList: List<String>, private val clickable: Boolean) : RecyclerView.Adapter<UpcomingEventsAdapter.ViewHolder>() {
    private var listener: OnClickListen? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (clickable) {
                listener!!.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.upcoming_event_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.UpcomingText.text = eventList[position]

    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setLongClickListener(listen: OnClickListen) {
        listener = listen
    }

    interface OnClickListen {
        fun onItemClick(view: View, position: Int)
    }

}
