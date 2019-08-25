package com.example.kocja.rabbiter_online.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.activities.AddEntryActivity
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.services.EventTriggered
import com.example.kocja.rabbiter_online.services.ProcessService
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import kotlinx.android.synthetic.main.fragment_upcoming_history_layout.*
import org.koin.android.viewmodel.ext.android.sharedViewModel


/**
 * Created by kocja on 27/02/2018.
 */

private const val ADD_ENTRY_EVENT = 5
class UpcomingEventsFragment : Fragment(), View.OnClickListener {
    private var noteToDisplay: List<String> = emptyList()
    private var eventList: List<Events> = emptyList()
    private val adapter: UpcomingEventsAdapter by lazy{ UpcomingEventsAdapter(noteToDisplay.toList())}
    private var lastItemClicked: Int = 0
    private val rabbitViewModel : RabbitViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val upcomingList = inflater.inflate(R.layout.fragment_upcoming_history_layout, container, false)
        updateNotesToDisplay {
            adapter.setLongClickListener(this)
            upcomingAdapter.layoutManager = LinearLayoutManager(context)
            upcomingAdapter.setHasFixedSize(true)
            upcomingAdapter.adapter = adapter
        }
        return upcomingList
    }

    override fun onClick(view: View) {
        val position = upcomingAdapter.getChildAdapterPosition(view)
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
                        startActivityForResult(yesIntent, ADD_ENTRY_EVENT)

                    } else {
                        val processEvents = Intent(context, ProcessService::class.java)
                        processEvents.putExtra("happened", true)
                        processEvents.putExtra("processEventUUID", eventList[position].eventUUID)
                        context?.startService(processEvents)
                    }
                    refreshFragment(upcomingAdapter, context)
                    updateNotesToDisplay { adapter.notifyDataSetChanged() }

                }
                .setNegativeButton("no") { _, _ ->
                    val noIntent = Intent(context, ProcessService::class.java)
                    noIntent.putExtra("processEventUUID", eventList[position].eventUUID)
                    noIntent.putExtra("happened", false)
                    requireContext().startService(noIntent)

                    refreshFragment(upcomingAdapter, context)

                }
                .setNeutralButton("cancel") { dialogInterface, _ -> dialogInterface.cancel() }
        builder.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ENTRY_EVENT) {
            refreshFragment(upcomingAdapter, context)
            updateNotesToDisplay {
                val processEvent = Intent(context, ProcessService::class.java)
                processEvent.putExtra("processEventUUID", eventList[lastItemClicked].eventUUID)
                processEvent.putExtra("getMode", EventTriggered.ADD_ENTRY_FROM_BIRTH)
                processEvent.putExtra("happened", true)
                requireContext().startService(processEvent)
                refreshFragment(upcomingAdapter, context)
            }


        }
    }

    fun refreshFragment(upcomingEvents: RecyclerView, context: Context?) {
        //this needs an update, should notifyDataSetChanged
        updateNotesToDisplay {
            upcomingEvents.invalidate()
            val adapter = UpcomingEventsAdapter(noteToDisplay)
            adapter.setLongClickListener(this)
            val manager = LinearLayoutManager(context)
            upcomingEvents.layoutManager = manager
            upcomingEvents.setHasFixedSize(true)
            upcomingEvents.adapter = adapter
        }
    }

    fun updateNotesToDisplay(onUpdate : ()->Unit) {
        rabbitViewModel.findNotAlertedEvents {events->
            eventList = events
            noteToDisplay = events.filter{it.eventString != null}.map{it.eventString!!}
            onUpdate()
        }
    }

}
