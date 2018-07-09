package com.example.kocja.rabbiter_online.fragments;

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

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter;
import com.example.kocja.rabbiter_online.databases.Entry;
import com.example.kocja.rabbiter_online.databases.Events;

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
        HttpManager.postRequest("seekPastEvents", GsonManager.getGson().toJson(entryName), (response,bytes) -> {
            //historyList = context.findViewById(R.id.upcomingList);
            Events[] events = GsonManager.getGson().fromJson(response,Events[].class);
            List<String> eventStrings = new ArrayList<>(events.length);
            for(Events event : events){
                eventStrings.add(event.getEventString());
            }
            setFragmentAdapter(eventStrings,context,view);

        });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.name.eq(entryName))
                .and(Events_Table.notificationState.notEq(Events.NOT_YET_ALERTED))
                .async()
                .queryListResultCallback((transaction, tResult) -> {

                }).execute();
        */
    }

    public static void maleParentOf(Context context, String parent, RecyclerView view, Activity activity){
        HttpManager.postRequest("seekParentOf", GsonManager.getGson().toJson(parent), (response,bytes) -> {
            Entry[] entries = GsonManager.getGson().fromJson(response,Entry[].class);
            List<String> parentOfList = new ArrayList<>(entries.length);
            for(Entry entry : entries){
                parentOfList.add(activity.getString(R.string.parentOf,entry.getEntryName()));
            }
            setFragmentAdapter(parentOfList,context,view);

        });
        /*
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.chooseGender.eq(activity.getString(R.string.genderMale)))
                .and(Entry_Table.matedWithOrParents.eq(parent))
                .or(Entry_Table.secondParent.eq(parent))
                .and(Entry_Table.chooseGender.eq(activity.getString(R.string.genderGroup)))
                .async()
                .queryListResultCallback((transaction, tResult) -> {

                }).execute();
         */
    }
    public static void setFragmentAdapter(List<String> stringList,Context c,RecyclerView view){
        HttpManager.handler.post(() -> {
            UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(stringList,false);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(c);
            view.setLayoutManager(manager);
            view.setHasFixedSize(true);
            view.setAdapter(adapter);
        });

    }
}
