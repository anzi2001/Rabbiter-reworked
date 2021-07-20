package com.example.kocja.rabbiter_online.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.activities.AddEntryActivity
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_online.databinding.FragmentUpcomingHistoryLayoutBinding
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.services.EventTriggered
import com.example.kocja.rabbiter_online.services.ProcessService
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


/**
 * Created by kocja on 27/02/2018.
 */

class UpcomingEventsFragment : Fragment(R.layout.fragment_upcoming_history_layout), View.OnClickListener {
    private var noteToDisplay: MutableList<String> = mutableListOf()
    private var eventList: MutableList<Events> = mutableListOf()
    private val adapter: UpcomingEventsAdapter by lazy{ UpcomingEventsAdapter(noteToDisplay)}
    private var lastItemClicked: Int = 0
    private val rabbitViewModel : RabbitViewModel by sharedViewModel()
    private var fragmentUpcomingHistoryLayoutBinding : FragmentUpcomingHistoryLayoutBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentUpcomingHistoryLayoutBinding = FragmentUpcomingHistoryLayoutBinding.inflate(layoutInflater,container,false)
        return fragmentUpcomingHistoryLayoutBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNotesToDisplay()
        adapter.setLongClickListener(this@UpcomingEventsFragment)
        with(fragmentUpcomingHistoryLayoutBinding!!) {
            upcomingAdapter.layoutManager = LinearLayoutManager(context)
            upcomingAdapter.setHasFixedSize(true)
            upcomingAdapter.adapter = adapter
        }

    }

    override fun onClick(view: View) {
        val position = fragmentUpcomingHistoryLayoutBinding!!.upcomingAdapter.getChildAdapterPosition(view)
        lastItemClicked = position
        val builder = AlertDialog.Builder(context)
                .setTitle("Event")
                .setMessage(noteToDisplay[position])
                .setPositiveButton("yes") { _, _ ->
                    if (eventList[position].typeOfEvent == Events.BIRTH_EVENT) {
                        val yesIntent = Intent(context, AddEntryActivity::class.java)
                        yesIntent.putExtra("eventUUID", eventList[position].eventUUID)
                        yesIntent.putExtra("getMode", EventTriggered.ADD_ENTRY_FROM_BIRTH)
                        yesIntent.putExtra("happened", true)
                        val addEntryEventResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                            refreshFragment()
                            updateNotesToDisplay()
                            val processEvent = Intent(context, ProcessService::class.java).apply {
                                putExtra("processEventUUID", eventList[lastItemClicked].eventUUID)
                                putExtra("getMode", EventTriggered.ADD_ENTRY_FROM_BIRTH)
                                putExtra("happened", true)
                            }
                            requireContext().startService(processEvent)
                            refreshFragment()
                        }
                        addEntryEventResult.launch(yesIntent)

                    } else {
                        val processEvents = Intent(context, ProcessService::class.java).apply {
                            putExtra("happened", true)
                            putExtra("processEventUUID", eventList[position].eventUUID)
                        }
                        context?.startService(processEvents)
                    }
                    refreshFragment()
                    updateNotesToDisplay()
                    adapter.notifyDataSetChanged()

                }
                .setNegativeButton("no") { _, _ ->
                    val noIntent = Intent(context, ProcessService::class.java)
                    noIntent.putExtra("processEventUUID", eventList[position].eventUUID)
                    noIntent.putExtra("happened", false)
                    requireContext().startService(noIntent)
                    refreshFragment()
                }
                .setNeutralButton("cancel") { dialogInterface, _ -> dialogInterface.cancel() }
        builder.show()

    }

    fun refreshFragment() {
        //this needs an update, should notifyDataSetChanged
        updateNotesToDisplay()
        adapter.notifyDataSetChanged()
    }

    fun updateNotesToDisplay() {
        lifecycleScope.launch(Dispatchers.IO){
            val events = rabbitViewModel.findNotAlertedEvents()
            eventList.clear()
            eventList.addAll(events)
            noteToDisplay.clear()
            noteToDisplay.addAll(events.mapNotNull { it.eventString })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentUpcomingHistoryLayoutBinding = null
    }
}
