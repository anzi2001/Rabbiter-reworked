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

import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.activities.AddEntryActivity
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_online.models.Events
import com.example.kocja.rabbiter_online.services.NotifyUser
import com.example.kocja.rabbiter_online.services.ProcessService
import kotlinx.android.synthetic.main.fragment_upcoming_history_layout.*


/**
 * Created by kocja on 27/02/2018.
 */

private const val ADD_ENTRY_EVENT = 5
class UpcomingEventsFragment : Fragment(), UpcomingEventsAdapter.OnClickListen {
    private var noteToDisplay: List<String> = emptyList()
    private var eventList: List<Events> = emptyList()
    private val adapter: UpcomingEventsAdapter by lazy{ UpcomingEventsAdapter(noteToDisplay.toList(),true)}
    private var lastItemClicked: Int = 0

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

    override fun onItemClick(view: View, position: Int) {
        lastItemClicked = position
        val builder = AlertDialog.Builder(context)
                .setTitle("Event")
                .setMessage(noteToDisplay[position])
                .setPositiveButton("yes") { _, _ ->
                    if (eventList[position].typeOfEvent == Events.BIRTH_EVENT) {
                        val yesIntent = Intent(context, AddEntryActivity::class.java)
                        yesIntent.putExtra("eventUUID", eventList[position].eventUUID)
                        yesIntent.putExtra("getMode", NotifyUser.ADD_ENTRY_FROM_BIRTH)
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
                processEvent.putExtra("getMode", NotifyUser.ADD_ENTRY_FROM_BIRTH)
                processEvent.putExtra("happened", true)
                requireContext().startService(processEvent)
                refreshFragment(upcomingAdapter, context)
            }


        }
    }

    fun refreshFragment(upcomingEvents: RecyclerView, context: Context?) {
        updateNotesToDisplay {
            upcomingEvents.invalidate()
            val adapter = UpcomingEventsAdapter(noteToDisplay, true)
            adapter.setLongClickListener(this)
            val manager = LinearLayoutManager(context)
            upcomingEvents.layoutManager = manager
            upcomingEvents.setHasFixedSize(true)
            upcomingEvents.adapter = adapter
        }
    }

    fun updateNotesToDisplay(onUpdate : ()->Unit) {
        HttpManager.getRequest("seekEventsNotAlerted") { response ->
            eventList = GsonManager.gson.fromJson(response, Array<Events>::class.java).toList()
            noteToDisplay = eventList.map { it.eventString }
            HttpManager.handler.post { onUpdate() }

        }
    }

}
