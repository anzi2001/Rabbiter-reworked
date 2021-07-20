package com.example.kocja.rabbiter_online.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_online.databinding.FragmentUpcomingHistoryLayoutBinding
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


/**
 * Created by kocja on 01/03/2018.
 */

class HistoryFragment : Fragment() {

    private val viewEntryViewModel : ViewEntryViewModel by sharedViewModel()
    var fragmentUpcomingHistoryLayoutBinding : FragmentUpcomingHistoryLayoutBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentUpcomingHistoryLayoutBinding = FragmentUpcomingHistoryLayoutBinding.inflate(layoutInflater,container,false)
        return fragmentUpcomingHistoryLayoutBinding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentUpcomingHistoryLayoutBinding = null
    }

    fun setPastEvents(context: Context, entryName: String, view: RecyclerView) {
        lifecycleScope.launch{
            val result = viewEntryViewModel.findPastEvents(entryName)
            val eventStrings = result.filter{it.eventString != null}.map{it.eventString!!}
            withContext(Dispatchers.Main){
                setFragmentAdapter(eventStrings, context, view)
            }
        }
    }

    fun maleParentOf(context: Context, parent: String, view: RecyclerView) {
        lifecycleScope.launch {
            val entries = viewEntryViewModel.findParentOf(parent)
            val parentOfList = entries.map{getString(R.string.parentOf,it.entryName)}
            withContext(Dispatchers.Main){
                setFragmentAdapter(parentOfList, context, view)
            }

        }

    }
    private fun setFragmentAdapter(stringList: List<String>, c: Context, view: RecyclerView) {
            view.layoutManager = LinearLayoutManager(c)
            view.setHasFixedSize(true)
            view.adapter = UpcomingEventsAdapter(stringList)
    }
}
