package com.example.kocja.rabbiter_reworked.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.example.kocja.rabbiter_reworked.services.AlertEventService;
import com.example.kocja.rabbiter_reworked.services.processEvents;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kocja on 27/02/2018.
 */

public class UpcomingEventsFragment extends Fragment implements UpcomingEventsAdapter.onClickListen{
    public static final int ADD_ENTRY_EVENT = 5;
    static List<String> noteToDisplay;
    static List<Events> eventList;
    RecyclerView upcomingAdapter;
    RecyclerView.LayoutManager manager;
    UpcomingEventsAdapter adapter;
    static UpcomingEventsAdapter.onClickListen listener;
    int lastItemClicked;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View upcomingList = inflater.inflate(R.layout.upcoming_history_fragment_layout,container,false);
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                //.or(Events_Table.yesClicked.eq(Events.EVENT_SUCCESSFUL))
                .orderBy(Events_Table.dateOfEvent,true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    eventList = events;
                    noteToDisplay = new ArrayList<>(events.size());
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }
                    listener = this;
                    upcomingAdapter = upcomingList.findViewById(R.id.upcomingAdapter);
                    adapter = new UpcomingEventsAdapter(noteToDisplay,true);
                    adapter.setLongClickListener(this);
                    manager = new LinearLayoutManager(getContext());
                    upcomingAdapter.setLayoutManager(manager);
                    upcomingAdapter.setHasFixedSize(true);
                    upcomingAdapter.setAdapter(adapter);

                }).execute();



        return upcomingList;
    }
    public static void refreshFragment(RecyclerView upcomingEvents,Context context){
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .orderBy(Events_Table.dateOfEvent, true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    eventList = events;
                    List<String> noteToDisplay = new ArrayList<>(events.size());
                    for (Events event : events) {
                        noteToDisplay.add(event.eventString);
                    }
                    upcomingEvents.invalidate();
                    UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(noteToDisplay,true);
                    adapter.setLongClickListener(listener);
                    RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
                    upcomingEvents.setLayoutManager(manager);
                    upcomingEvents.setHasFixedSize(true);
                    upcomingEvents.setAdapter(adapter);


                }).execute();
    }

    public static void updateNotesToDisplay(){
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .orderBy(Events_Table.dateOfEvent,true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    noteToDisplay = new ArrayList<>(events.size());
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }

                }).execute();
    }

    @Override
    public void onItemClick(View view, int position) {
        lastItemClicked = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Event")
                .setMessage(noteToDisplay.get(position))
                .setPositiveButton("yes", (dialogInterface, i1) -> {
                    if(eventList.get(position).typeOfEvent == 0){
                        Intent yesIntent = new Intent(getContext(), addEntryActivity.class);
                        yesIntent.putExtra("eventUUID", eventList.get(position).eventUUID);
                        yesIntent.putExtra("getMode", AlertEventService.ADD_BIRTH_FROM_SERVICE);
                        yesIntent.putExtra("happened", true);
                        startActivityForResult(yesIntent,ADD_ENTRY_EVENT);

                        refreshFragment(upcomingAdapter,getContext());
                    }
                    else{
                        Intent processEvents = new Intent(getContext(), com.example.kocja.rabbiter_reworked.services.processEvents.class);
                        processEvents.putExtra("happened", true);
                        processEvents.putExtra("processEventUUID", eventList.get(position).eventUUID);
                        getContext().startService(processEvents);

                        refreshFragment(upcomingAdapter,getContext());

                    }
                    updateNotesToDisplay();
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("no", (dialogInterface, i12) -> {
                    Intent noIntent = new Intent(getContext(),processEvents.class);
                    noIntent.putExtra("processEventUUID",eventList.get(position).eventUUID);
                    noIntent.putExtra("happened",false);
                    getContext().startService(noIntent);

                    //updateNotesToDisplay();
                    //adapter.notifyDataSetChanged();
                    refreshFragment(upcomingAdapter,getContext());

                })
                .setNeutralButton("cancel", (dialogInterface, i13) -> dialogInterface.cancel());
        builder.show();

    }
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == ADD_ENTRY_EVENT) {
            refreshFragment(upcomingAdapter,getContext());
            updateNotesToDisplay();
            Intent processEvent = new Intent(getContext(),processEvents.class);
            processEvent.putExtra("processEventUUID",eventList.get(lastItemClicked).eventUUID);
            processEvent.putExtra("getMode",AlertEventService.ADD_BIRTH_FROM_SERVICE);
            processEvent.putExtra("happened",true);
            getContext().startService(processEvent);
            refreshFragment(upcomingAdapter,getContext());

        }
    }
}
