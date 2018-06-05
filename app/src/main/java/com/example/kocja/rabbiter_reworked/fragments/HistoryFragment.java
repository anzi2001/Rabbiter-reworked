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

import com.example.kocja.rabbiter_reworked.GsonManager;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.SocketIOManager;
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.google.gson.JsonObject;

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
        SocketIOManager.getSocket().emit("seekPastEventsReq",entryName);
        SocketIOManager.getSocket().on("seekPastEventsRes", args -> {
            //historyList = context.findViewById(R.id.upcomingList);
            List<String> eventStrings = new ArrayList<>(args.length);
            for(Object eventObj : args){
                Events event = GsonManager.getGson().fromJson((JsonObject)eventObj,Events.class);
                eventStrings.add(event.eventString);
            }
            UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(eventStrings,false);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
            view.setLayoutManager(manager);
            view.setHasFixedSize(true);
            view.setAdapter(adapter);
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
        SocketIOManager.getSocket().emit("seekParentOfReq",parent);
        SocketIOManager.getSocket().on("seekParentOfRes", args -> {
            List<String> parentOfList = new ArrayList<>(args.length);
            for(Object entryObj : args){
                Entry entry =  GsonManager.getGson().fromJson((JsonObject)entryObj,Entry.class);
                parentOfList.add(activity.getString(R.string.parentOf,entry.entryName));
            }
            UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(parentOfList,false);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
            view.setLayoutManager(manager);
            view.setHasFixedSize(true);
            view.setAdapter(adapter);
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
}
