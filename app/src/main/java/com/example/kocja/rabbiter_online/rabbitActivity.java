package com.example.kocja.rabbiter_online;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_online.activities.addEntryActivity;
import com.example.kocja.rabbiter_online.activities.viewEntry;
import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter;
import com.example.kocja.rabbiter_online.databases.Entry;
import com.example.kocja.rabbiter_online.fragments.UpcomingEventsFragment;
import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.services.alertIfNotAlertedService;
import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class rabbitActivity extends AppCompatActivity implements EntriesRecyclerAdapter.onItemClickListener,fillData.onPost{
    private static final int ADD_ENTRY_START = 0;
    private static final int START_VIEW_ENTRY = 1;
    private static final int START_PERMISSION_REQUEST =2;
    private int chosenEntriesCounter = 0;
    private RecyclerView rabbitEntryView;
    private List<Entry> entriesList;
    private Entry firstMergeEntry = null;
    private Entry secondMergeEntry = null;
    private FloatingActionButton mergeFab;
    private FloatingActionButton splitFab;
    private Gson gson;
    private boolean wasMergedBefore = false;
    private Entry secondMerge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rabbit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GsonManager.initGson();
        gson = GsonManager.getGson();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},START_PERMISSION_REQUEST);

        }

        Intent checkAlarms = new Intent(this,alertIfNotAlertedService.class);
        startService(checkAlarms);

        FloatingActionButton addFab = findViewById(R.id.addFab);
        mergeFab = findViewById(R.id.mergeFab);
        splitFab = findViewById(R.id.splitFab);
        addFab.setOnClickListener(view -> {
            Intent addEntryIntent = new Intent(this,addEntryActivity.class);
            startActivityForResult(addEntryIntent, ADD_ENTRY_START);
        });
        rabbitEntryView = findViewById(R.id.rabbitEntryView);
        rabbitEntryView.setHasFixedSize(true);
        RecyclerView.LayoutManager rabbitEntryManager = new GridLayoutManager(this, 3);
        rabbitEntryView.setLayoutManager(rabbitEntryManager);
        fillData.getEntries(this, rabbitEntryView, this,this);

        mergeFab.setOnClickListener(view -> {
            if(chosenEntriesCounter > 2){
                chosenEntriesCounter--;
                Toast.makeText(rabbitActivity.this,R.string.alertMoreThan2Chosen,Toast.LENGTH_LONG).show();
                return;
            }
            secondMergeEntry.setMerged(true);
            secondMergeEntry.setMergedEntryName(firstMergeEntry.getEntryName());
            secondMergeEntry.setMergedEntry(firstMergeEntry.getEntryID().toString());
            secondMergeEntry.setMergedEntryPhLoc(firstMergeEntry.getEntryPhLoc());
            HttpManager.postRequest("updateEntry",gson.toJson(secondMergeEntry), (response, bytes) -> { });
            firstMergeEntry.setChildMerged(true);
            HttpManager.postRequest("updateEntry", gson.toJson(firstMergeEntry), (response,bytes) -> { });

            //reset and refresh the grid at the end
            animateDown(mergeFab);
            chosenEntriesCounter = 0;
            firstMergeEntry = null;
            secondMergeEntry = null;

            fillData.getEntries(rabbitActivity.this, rabbitEntryView, this, this);
        });
        splitFab.setOnClickListener(view -> {
            firstMergeEntry.setMerged(false);

            HttpManager.postRequest("updateEntry", gson.toJson(firstMergeEntry), (response,bytes) -> { });

            HttpManager.postRequest("seekSingleEntry", gson.toJson(firstMergeEntry.getMergedEntry()), (response,bytes) -> {
                secondMerge = gson.fromJson(response, Entry[].class)[0];
                secondMerge.setChildMerged(false);
                HttpManager.postRequest("updateEntry", gson.toJson(secondMerge), (response1,bytes1) -> {
                    chosenEntriesCounter =0;
                    firstMergeEntry = null;
                    secondMergeEntry = null;
                    this.runOnUiThread(() -> {
                        animateDown(splitFab);
                        fillData.getEntries(rabbitActivity.this, rabbitEntryView, this,this);
                    });

                });
            });

            //reset and refresh the grid at the end

        });

    }

    private static void animateDown(FloatingActionButton toMove){
        toMove.animate().translationY(200);
    }
    private static void animateUp(FloatingActionButton toMove){
        toMove.animate().translationY(-100);
    }

    @Override
    public void onActivityResult(int requestCode,int resultcode, Intent data){
        super.onActivityResult(requestCode,resultcode,data);
        if(requestCode == ADD_ENTRY_START && resultcode == RESULT_OK){
            fillData.getEntries(this, rabbitEntryView, this,this);
            RecyclerView upcomingEvents = findViewById(R.id.upcomingAdapter);
            UpcomingEventsFragment.refreshFragment(upcomingEvents,this);
            UpcomingEventsFragment.updateNotesToDisplay(() -> { });
        }
        else if(requestCode == START_VIEW_ENTRY){
            fillData.getEntries(this, rabbitEntryView, this,this);
            UpcomingEventsFragment.updateNotesToDisplay(() -> { });
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent startViewEntry = new Intent(this,viewEntry.class);
        startViewEntry.putExtra("entryID",(UUID)view.getTag());
        startActivityForResult(startViewEntry,START_VIEW_ENTRY);
    }

    @Override
    public void onLongItemClick(View view, int position) {
        CircleImageView markedOrNot = view.findViewById(R.id.MarkedOrNot);
        if(markedOrNot.getDrawable() == null){
            Glide.with(this).load(R.drawable.ic_markedornot).into(markedOrNot);
        }
        if(markedOrNot.getVisibility() == View.GONE) {
            chosenEntriesCounter++;

            //Since we're adding a new entry we're passing the old one to the second one,
            //since it became a second one now

            secondMergeEntry = firstMergeEntry;
            firstMergeEntry = entriesList.get(position);

            if(firstMergeEntry.isMerged() && chosenEntriesCounter < 2){
                animateUp(splitFab);
            }

            markedOrNot.setVisibility(View.VISIBLE);
            // if both are not null we can safely merge the 2 chosen
            if(secondMergeEntry != null /*&& firstMergeEntry != null*/){
                animateUp(mergeFab);
                wasMergedBefore = true;
            }
        }
        else{
            chosenEntriesCounter--;
            markedOrNot.setVisibility(View.GONE);
            if(firstMergeEntry.isMerged()){
                animateDown(splitFab);
            }

            //If we're deselecting the entry, the second entry became the first, since
            //we now have only 1 entry
            firstMergeEntry = secondMergeEntry;
            secondMergeEntry= null;

            if(chosenEntriesCounter < 2 && wasMergedBefore){
                animateDown(mergeFab);
                wasMergedBefore = false;
            }

        }

    }

    @Override
    public void onPostProcess(List<Entry> temporaryList) {
        entriesList = temporaryList;
    }
}
