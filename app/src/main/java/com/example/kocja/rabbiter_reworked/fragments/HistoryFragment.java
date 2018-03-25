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
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kocja on 01/03/2018.
 */

public class HistoryFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.upcoming_history_fragment_layout, container, false);
    }
    public static void setPastEvents(Context context,String entryName,ListView view){
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.name.eq(entryName))
                .and(Events_Table.notificationState.notEq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    //historyList = context.findViewById(R.id.upcomingList);
                    List<String> eventStrings = new ArrayList<>(tResult.size());
                    for(Events event : tResult){
                        eventStrings.add(event.eventString);
                    }
                    ListAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, eventStrings);
                    view.setAdapter(adapter);
                }).execute();
    }

    public static void maleParentOf(Context context, String parent,ListView view){
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.matedWithOrParents.eq(parent))
                .or(Entry_Table.secondParent.eq(parent))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    List<String> parentOfList = new ArrayList<>(tResult.size());
                    for(Entry entry : tResult){
                        parentOfList.add("Parent of: " + entry.entryName);
                    }
                    ListAdapter adapter = new ArrayAdapter<>(context,android.R.layout.simple_list_item_1,parentOfList);
                    view.setAdapter(adapter);
                }).execute();
    }
}
