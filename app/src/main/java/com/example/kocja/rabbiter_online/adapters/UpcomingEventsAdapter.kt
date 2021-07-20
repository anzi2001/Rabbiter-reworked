package com.example.kocja.rabbiter_online.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kocja.rabbiter_online.databinding.UpcomingEventLayoutBinding


class UpcomingEventsAdapter(private val eventList: List<String>) : RecyclerView.Adapter<UpcomingEventsAdapter.ViewHolder>() {
    private var listener: View.OnClickListener? = null


    inner class ViewHolder(val item: UpcomingEventLayoutBinding) : RecyclerView.ViewHolder(item.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = UpcomingEventLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        if(listener != null) {
            view.root.setOnClickListener(listener)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item.UpcomingText.text = eventList[position]
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    fun setLongClickListener(listen: View.OnClickListener) {
        listener = listen
    }

}
