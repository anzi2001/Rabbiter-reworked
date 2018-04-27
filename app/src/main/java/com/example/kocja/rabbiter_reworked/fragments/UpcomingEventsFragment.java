package com.example.kocja.rabbiter_reworked.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.example.kocja.rabbiter_reworked.services.AlertEventService;
import com.example.kocja.rabbiter_reworked.services.askNotifAgain;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kocja on 27/02/2018.
 */

public class UpcomingEventsFragment extends Fragment {
    List<String> noteToDisplay;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View upcomingList = inflater.inflate(R.layout.upcoming_history_fragment_layout,container,false);
        ListView upcomingEvents = upcomingList.findViewById(R.id.upcomingList);
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                //.or(Events_Table.yesClicked.eq(Events.EVENT_SUCCESSFUL))
                .orderBy(Events_Table.dateOfEvent,true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    noteToDisplay = new ArrayList<>(events.size());
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }

                    ListAdapter listAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,noteToDisplay);
                    upcomingEvents.setAdapter(listAdapter);
                    upcomingEvents.setOnItemClickListener((adapterView, view, i, l) -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                .setTitle("Event")
                                .setMessage(noteToDisplay.get(i))
                                .setPositiveButton("yes", (dialogInterface, i1) -> {
                                    if(events.get(i).typeOfEvent == 0){
                                        Intent yesIntent = new Intent(getContext(), addEntryActivity.class);
                                        yesIntent.putExtra("eventUUID", events.get(i).eventUUID);
                                        yesIntent.putExtra("getMode", AlertEventService.ADD_BIRTH_FROM_SERVICE);
                                        yesIntent.putExtra("happened", true);
                                        getContext().startActivity(yesIntent);

                                    }
                                    else{
                                        Intent processEvents = new Intent(getContext(), com.example.kocja.rabbiter_reworked.services.processEvents.class);
                                        processEvents.putExtra("happened", true);
                                        processEvents.putExtra("processEventUUID", events.get(i).eventUUID);
                                        getContext().startService(processEvents);

                                    }
                                    noteToDisplay = updateNotesToDisplay(events.size());
                                    ListAdapter refreshAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,noteToDisplay);
                                    upcomingEvents.setAdapter(refreshAdapter);

                                })
                                .setNegativeButton("no", (dialogInterface, i12) -> {
                                        Intent noIntent = new Intent(getContext(),askNotifAgain.class);
                                        noIntent.putExtra("eventUUID",events.get(i).eventUUID);
                                        getContext().startService(noIntent);

                                        noteToDisplay = updateNotesToDisplay(events.size());
                                    ListAdapter refreshAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,noteToDisplay);
                                    upcomingEvents.setAdapter(refreshAdapter);

                                })
                                .setNeutralButton("cancel", (dialogInterface, i13) -> dialogInterface.cancel());
                        builder.show();

                    });
                }).execute();


        return upcomingList;
    }
    public static void refreshFragment(ListView upcomingEvents, Context context){
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .orderBy(Events_Table.dateOfEvent, true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    List<String> noteToDisplay = new ArrayList<>(events.size());
                    for (Events event : events) {
                        noteToDisplay.add(event.eventString);
                    }
                    ListAdapter listAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, noteToDisplay);
                    upcomingEvents.setAdapter(listAdapter);
                }).execute();
    }
    private static List<String> updateNotesToDisplay(int eventSize){
        List<String> noteToDisplay = new ArrayList<>(eventSize);
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .orderBy(Events_Table.dateOfEvent,true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }
                }).execute();

        return noteToDisplay;
    }
}
