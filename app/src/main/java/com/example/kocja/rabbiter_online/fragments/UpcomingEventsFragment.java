package com.example.kocja.rabbiter_online.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.activities.addEntryActivity;
import com.example.kocja.rabbiter_online.adapters.UpcomingEventsAdapter;
import com.example.kocja.rabbiter_online.databases.Events;
import com.example.kocja.rabbiter_online.services.NotifyUser;
import com.example.kocja.rabbiter_online.services.ProcessService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Created by kocja on 27/02/2018.
 */

public class UpcomingEventsFragment extends Fragment implements UpcomingEventsAdapter.onClickListen{
    private static final int ADD_ENTRY_EVENT = 5;
    private static List<String> noteToDisplay;
    private static List<Events> eventList;
    private RecyclerView upcomingAdapter;
    private RecyclerView.LayoutManager manager;
    private UpcomingEventsAdapter adapter;
    private static UpcomingEventsAdapter.onClickListen listener;
    private int lastItemClicked;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View upcomingList = inflater.inflate(R.layout.upcoming_history_fragment_layout,container,false);
        updateNotesToDisplay(() -> {
            listener = this;
            upcomingAdapter = upcomingList.findViewById(R.id.upcomingAdapter);
            adapter = new UpcomingEventsAdapter(noteToDisplay,true);
            adapter.setLongClickListener(this);
            manager = new LinearLayoutManager(getContext());
            upcomingAdapter.setLayoutManager(manager);
            upcomingAdapter.setHasFixedSize(true);
            upcomingAdapter.setAdapter(adapter); });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                //.or(Events_Table.yesClicked.eq(Events.EVENT_SUCCESSFUL))
                .orderBy(Events_Table.dateOfEvent,true)
                .async()
                .queryListResultCallback((transaction, events) -> {

                }).execute();
        */
        return upcomingList;
    }
    public static void refreshFragment(RecyclerView upcomingEvents,Context context){
        updateNotesToDisplay(() -> {
            upcomingEvents.invalidate();
            UpcomingEventsAdapter adapter = new UpcomingEventsAdapter(noteToDisplay, true);
            adapter.setLongClickListener(listener);
            RecyclerView.LayoutManager manager = new LinearLayoutManager(context);
            upcomingEvents.setLayoutManager(manager);
            upcomingEvents.setHasFixedSize(true);
            upcomingEvents.setAdapter(adapter);
        });
        /*
                SQLite.select()
                        .from(Events.class)
                        .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                        .orderBy(Events_Table.dateOfEvent, true)
                        .async()
                        .queryListResultCallback((transaction, events) -> {

                        }).execute();
          */
    }

    public static void updateNotesToDisplay(onUpdate onUpdate){
        HttpManager.INSTANCE.getRequest("seekEventsNotAlerted", response -> {
                eventList = Arrays.asList(GsonManager.INSTANCE.getGson().fromJson(response,Events[].class));
                noteToDisplay = new ArrayList<>(eventList.size());
                for(Events event : eventList){
                    noteToDisplay.add(event.getEventString());
                }
                HttpManager.INSTANCE.getHandler().post(onUpdate::onNotesUpdate);


        });
        /*
        SQLite.select()
                .from(Events.class)
                .where(Events_Table.notificationState.eq(Events.NOT_YET_ALERTED))
                .orderBy(Events_Table.dateOfEvent,true)
                .async()
                .queryListResultCallback((transaction, events) -> {

                }).execute();
         */
    }

    @Override
    public void onItemClick(View view, int position) {
        lastItemClicked = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("Event")
                .setMessage(noteToDisplay.get(position))
                .setPositiveButton("yes", (dialogInterface, i1) -> {
                    if(eventList.get(position).getTypeOfEvent() == Events.BIRTH_EVENT){
                        Intent yesIntent = new Intent(getContext(), addEntryActivity.class);
                        yesIntent.putExtra("eventUUID", eventList.get(position).getEventUUID());
                        yesIntent.putExtra("getMode", NotifyUser.ADD_ENTRY_FROM_BIRTH);
                        yesIntent.putExtra("happened", true);
                        startActivityForResult(yesIntent,ADD_ENTRY_EVENT);

                        refreshFragment(upcomingAdapter,getContext());
                    }
                    else{
                        Intent processEvents = new Intent(getContext(), ProcessService.class);
                        processEvents.putExtra("happened", true);
                        processEvents.putExtra("processEventUUID", eventList.get(position).getEventUUID());
                        requireContext().startService(processEvents);

                        refreshFragment(upcomingAdapter,getContext());

                    }
                    updateNotesToDisplay(() -> adapter.notifyDataSetChanged());

                })
                .setNegativeButton("no", (dialogInterface, i12) -> {
                    Intent noIntent = new Intent(getContext(),ProcessService.class);
                    noIntent.putExtra("processEventUUID",eventList.get(position).getEventUUID());
                    noIntent.putExtra("happened",false);
                    requireContext().startService(noIntent);

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
            updateNotesToDisplay(() -> {
                Intent processEvent = new Intent(getContext(),ProcessService.class);
                processEvent.putExtra("processEventUUID",eventList.get(lastItemClicked).getEventUUID());
                processEvent.putExtra("getMode",NotifyUser.ADD_ENTRY_FROM_BIRTH);
                processEvent.putExtra("happened",true);
                requireContext().startService(processEvent);
                refreshFragment(upcomingAdapter,getContext());
            });


        }
    }
    public interface onUpdate{
        void onNotesUpdate();
    }
}
