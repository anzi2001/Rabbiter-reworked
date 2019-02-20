package com.example.kocja.rabbiter_reworked.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.example.kocja.rabbiter_reworked.R

import java.util.ArrayList


class UpcomingEventsAdapter(events: List<String>, private val clickable: Boolean) : RecyclerView.Adapter<UpcomingEventsAdapter.viewHolder>() {
    private var eventList: List<String>? = null
    private var listener: onClickListen? = null

    init {
        eventList = ArrayList(events.size)
        eventList = events
    }

    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val eventString: TextView = itemView.findViewById(R.id.UpcomingText)

        init {
            eventString.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (clickable) {
                listener!!.onItemClick(view, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.upcoming_event_layout, parent, false)
        return viewHolder(view)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.eventString.text = eventList!![position]

    }

    override fun getItemCount(): Int {
        return eventList!!.size
    }

    fun setLongClickListener(listen: onClickListen) {
        listener = listen
    }

    interface onClickListen {
        fun onItemClick(view: View, position: Int)
    }

}
