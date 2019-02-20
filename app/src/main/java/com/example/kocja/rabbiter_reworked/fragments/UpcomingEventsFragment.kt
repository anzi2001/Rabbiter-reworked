package com.example.kocja.rabbiter_reworked.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.activities.AddEntryActivity
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.example.kocja.rabbiter_reworked.services.AlertEventService
import com.example.kocja.rabbiter_reworked.services.processEvents
import com.raizlabs.android.dbflow.sql.language.SQLite

import java.util.ArrayList

/**
 * Created by kocja on 27/02/2018.
 */

class UpcomingEventsFragment : Fragment(), UpcomingEventsAdapter.onClickListen {
    private var upcomingAdapter: RecyclerView? = null
    private var manager: RecyclerView.LayoutManager? = null
    private var adapter: UpcomingEventsAdapter? = null
    private var lastItemClicked: Int = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val upcomingList = inflater.inflate(R.layout.upcoming_history_fragment_layout, container, false)
        SQLite.select()
                .from(Events::class.java)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                //.or(Events_Table.yesClicked.eq(Events.EVENT_SUCCESSFUL))
                .orderBy(Events_Table.dateOfEvent, true)
                .async()
                .queryListResultCallback { _, events ->
                    eventList = events
                    noteToDisplay = ArrayList(events.size)
                    for (event in events) {
                        noteToDisplay!!.add(event.eventString)
                    }
                    listener = this
                    upcomingAdapter = upcomingList.findViewById(R.id.upcomingAdapter)
                    adapter = UpcomingEventsAdapter(noteToDisplay!!, true)
                    adapter!!.setLongClickListener(this)
                    manager = LinearLayoutManager(context)
                    upcomingAdapter!!.layoutManager = manager
                    upcomingAdapter!!.setHasFixedSize(true)
                    upcomingAdapter!!.adapter = adapter

                }.execute()



        return upcomingList
    }

    override fun onItemClick(view: View, position: Int) {
        lastItemClicked = position
        val builder = AlertDialog.Builder(context)
                .setTitle("Event")
                .setMessage(noteToDisplay!![position])
                .setPositiveButton("yes") { _, _ ->
                    if (eventList!![position].typeOfEvent == 0) {
                        val yesIntent = Intent(context, AddEntryActivity::class.java)
                        yesIntent.putExtra("eventUUID", eventList!![position].eventUUID)
                        yesIntent.putExtra("getMode", AlertEventService.ADD_BIRTH_FROM_SERVICE)
                        yesIntent.putExtra("happened", true)
                        startActivityForResult(yesIntent, ADD_ENTRY_EVENT)

                        refreshFragment(upcomingAdapter, context)
                    } else {
                        val processEvents = Intent(context, com.example.kocja.rabbiter_reworked.services.processEvents::class.java)
                        processEvents.putExtra("happened", true)
                        processEvents.putExtra("processEventUUID", eventList!![position].eventUUID)
                        context!!.startService(processEvents)

                        refreshFragment(upcomingAdapter, context)

                    }
                    updateNotesToDisplay()
                    adapter!!.notifyDataSetChanged()
                }
                .setNegativeButton("no") { _, _ ->
                    val noIntent = Intent(context, processEvents::class.java)
                    noIntent.putExtra("processEventUUID", eventList!![position].eventUUID)
                    noIntent.putExtra("happened", false)
                    context!!.startService(noIntent)

                    //updateNotesToDisplay();
                    //adapter.notifyDataSetChanged();
                    refreshFragment(upcomingAdapter, context)

                }
                .setNeutralButton("cancel") { dialogInterface, _ -> dialogInterface.cancel() }
        builder.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_ENTRY_EVENT) {
            refreshFragment(upcomingAdapter, context)
            updateNotesToDisplay()
            val processEvent = Intent(context, processEvents::class.java)
            processEvent.putExtra("processEventUUID", eventList!![lastItemClicked].eventUUID)
            processEvent.putExtra("getMode", AlertEventService.ADD_BIRTH_FROM_SERVICE)
            processEvent.putExtra("happened", true)
            context!!.startService(processEvent)
            refreshFragment(upcomingAdapter, context)

        }
    }

    companion object {
        const val ADD_ENTRY_EVENT = 5
        private var noteToDisplay: MutableList<String>? = null
        private var eventList: List<Events>? = null
        private var listener: UpcomingEventsAdapter.onClickListen? = null
        fun refreshFragment(upcomingEvents: RecyclerView?, context: Context?) {
            SQLite.select()
                    .from(Events::class.java)
                    .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                    .orderBy(Events_Table.dateOfEvent, true)
                    .async()
                    .queryListResultCallback { _, events ->
                        eventList = events
                        val noteToDisplay = ArrayList<String>(events.size)
                        for (event in events) {
                            noteToDisplay.add(event.eventString)
                        }
                        upcomingEvents!!.invalidate()
                        val adapter = UpcomingEventsAdapter(noteToDisplay, true)
                        adapter.setLongClickListener(listener!!)
                        val manager = LinearLayoutManager(context)
                        upcomingEvents.layoutManager = manager
                        upcomingEvents.setHasFixedSize(true)
                        upcomingEvents.adapter = adapter


                    }.execute()
        }

        fun updateNotesToDisplay() {
            SQLite.select()
                    .from(Events::class.java)
                    .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                    .orderBy(Events_Table.dateOfEvent, true)
                    .async()
                    .queryListResultCallback { _, events ->
                        noteToDisplay = ArrayList(events.size)
                        for (event in events) {
                            noteToDisplay!!.add(event.eventString)
                        }

                    }.execute()
        }
    }
}
