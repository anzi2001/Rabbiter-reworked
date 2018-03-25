package com.example.kocja.rabbiter_reworked.fragments;

import android.content.Context;
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
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kocja on 27/02/2018.
 */

public class UpcomingEventsFragment extends Fragment {
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
                    List<String> noteToDisplay = new ArrayList<>(events.size());
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }
                    ListAdapter listAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,noteToDisplay);
                    upcomingEvents.setAdapter(listAdapter);
                }).execute();


        return upcomingList;
    }
    public static void refreshFragment(ListView upcomingEvents, Context context){
        /*SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .orderBy(Events_Table.dateOfEvent, true)
                .async()
                .queryListResultCallback((transaction, events) -> {
                    List<String> noteToDisplay = new ArrayList<>(events.size());
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }
                    ListAdapter listAdapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,noteToDisplay);
                    upcomingEvents.setAdapter(listAdapter);*/
        upcomingEvents.deferNotifyDataSetChanged();
    }
}
