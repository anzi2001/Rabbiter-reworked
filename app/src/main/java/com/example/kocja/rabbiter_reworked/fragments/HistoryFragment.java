package com.example.kocja.rabbiter_reworked.fragments;

import android.app.Activity;
import android.content.Context;
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
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter;
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
    public static void setPastEvents(Context context, String entryName,RecyclerView view){
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
                    UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(eventStrings,false);
                    RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
                    view.setLayoutManager(manager);
                    view.setHasFixedSize(true);
                    view.setAdapter(adapter);
                }).execute();
    }

    public static void maleParentOf(Context context, String parent, RecyclerView view, Activity activity){
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.chooseGender.eq(activity.getString(R.string.genderMale)))
                .and(Entry_Table.matedWithOrParents.eq(parent))
                .or(Entry_Table.secondParent.eq(parent))
                .and(Entry_Table.chooseGender.eq(activity.getString(R.string.genderGroup)))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    List<String> parentOfList = new ArrayList<>(tResult.size());
                    for(Entry entry : tResult){
                        parentOfList.add(activity.getString(R.string.parentOf,entry.entryName));
                    }
                    UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(parentOfList,false);
                    RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
                    view.setLayoutManager(manager);
                    view.setHasFixedSize(true);
                    view.setAdapter(adapter);
                }).execute();
    }
}
