package com.example.kocja.rabbiter_reworked.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter
import com.example.kocja.rabbiter_reworked.databases.Entry
import com.example.kocja.rabbiter_reworked.databases.Entry_Table
import com.example.kocja.rabbiter_reworked.databases.Events
import com.example.kocja.rabbiter_reworked.databases.Events_Table
import com.raizlabs.android.dbflow.sql.language.SQLite

import java.util.ArrayList

/**
 * Created by kocja on 01/03/2018.
 */

class HistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.upcoming_history_fragment_layout, container, false)
    }

    companion object {
        fun setPastEvents(context: Context, entryName: String, view: RecyclerView) {
            SQLite.select()
                    .from(Events::class.java)
                    .where(Events_Table.name.eq(entryName))
                    .and(Events_Table.notificationState.notEq(Events.NOT_YET_ALERTED))
                    .async()
                    .queryListResultCallback { _, tResult ->
                        //historyList = context.findViewById(R.id.upcomingList);
                        val eventStrings = ArrayList<String>(tResult.size)
                        for (event in tResult) {
                            eventStrings.add(event.eventString)
                        }
                        val adapter = UpcomingEventsAdapter(eventStrings, false)
                        val manager = LinearLayoutManager(context)
                        view.layoutManager = manager
                        view.setHasFixedSize(true)
                        view.adapter = adapter
                    }.execute()
        }

        fun maleParentOf(context: Context, parent: String, view: RecyclerView, activity: Activity) {
            SQLite.select()
                    .from(Entry::class.java)
                    .where(Entry_Table.chooseGender.eq(activity.getString(R.string.genderMale)))
                    .and(Entry_Table.matedWithOrParents.eq(parent))
                    .or(Entry_Table.secondParent.eq(parent))
                    .and(Entry_Table.chooseGender.eq(activity.getString(R.string.genderGroup)))
                    .async()
                    .queryListResultCallback { _, tResult ->
                        val parentOfList = ArrayList<String>(tResult.size)
                        for (entry in tResult) {
                            parentOfList.add(activity.getString(R.string.parentOf, entry.entryName))
                        }
                        val adapter = UpcomingEventsAdapter(parentOfList, false)
                        val manager = LinearLayoutManager(context)
                        view.layoutManager = manager
                        view.setHasFixedSize(true)
                        view.adapter = adapter
                    }.execute()
        }
    }
}
