package com.example.kocja.rabbiter_online.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_online.extensions.observeOnce
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel
import java.util.*


/**
 * Created by kocja on 01/03/2018.
 */

class HistoryFragment : Fragment() {

    val viewEntryViewModel : ViewEntryViewModel by sharedViewModel()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_upcoming_history_layout, container, false)
    }

    fun setPastEvents(context: Context, entryName: String, view: RecyclerView) {
        viewEntryViewModel.findPastEvents(entryName).observeOnce(this, Observer {events->
            val eventStrings = events.map{it.eventString}
            setFragmentAdapter(eventStrings, context, view)
        })
    }

    fun maleParentOf(context: Context, parent: String, view: RecyclerView) {
        viewEntryViewModel.findParentOf(parent).observeOnce(this, Observer {entries->
            val parentOfList = entries.map{getString(R.string.parentOf,it.entryName)}
            setFragmentAdapter(parentOfList, context, view)
        })
    }
    private fun setFragmentAdapter(stringList: List<String>, c: Context, view: RecyclerView) {
            view.layoutManager = LinearLayoutManager(c)
            view.setHasFixedSize(true)
            view.adapter = UpcomingEventsAdapter(stringList, false)
    }
}
