package com.example.kocja.rabbiter_reworked.fragments;

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
                .where(Events_Table.yesClicked.eq(false))
                .async()
                .queryListResultCallback((transaction, events) -> {
                    List<String> noteToDisplay = new ArrayList<>(events.size());
                    for(Events event : events){
                        noteToDisplay.add(event.eventString);
                    }
                    ListAdapter listAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,noteToDisplay);
                    upcomingEvents.setAdapter(listAdapter);
                }).execute();


        return upcomingList;
    }
}
