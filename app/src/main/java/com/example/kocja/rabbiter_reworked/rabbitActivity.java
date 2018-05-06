package com.example.kocja.rabbiter_reworked;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.activities.viewEntry;
import com.example.kocja.rabbiter_reworked.adapters.EntriesRecyclerAdapter;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.fragments.UpcomingEventsFragment;
import com.example.kocja.rabbiter_reworked.services.alertIfNotAlertedService;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class rabbitActivity extends AppCompatActivity implements EntriesRecyclerAdapter.onItemClickListener {
    private static final int ADD_ENTRY_START = 0;
    public static final int START_VIEW_ENTRY = 1;
    private static final int START_PERMISSION_REQUEST =2;
    private int chosenEntriesCounter = 0;
    private RecyclerView rabbitEntryView;
    private List<Entry> entriesList;
    private Entry firstMergeEntry = null;
    private Entry secondMergeEntry = null;
    FloatingActionButton addFab;
    FloatingActionButton mergeFab;
    FloatingActionButton splitFab;

    private boolean wasMergedBefore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rabbit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},START_PERMISSION_REQUEST);

        }

        Intent checkAlarms = new Intent(this,alertIfNotAlertedService.class);
        startService(checkAlarms);

        addFab =  findViewById(R.id.addFab);
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
        entriesList = fillData.getEntries(this,rabbitEntryView,this);


        /*rabbitEntryView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            CircleImageView markedOrNot = view.findViewById(R.id.MarkedOrNot);
            if(markedOrNot.getDrawable() == null){
                Glide.with(this).load(R.drawable.ic_markedornot).into(markedOrNot);
            }
            if(markedOrNot.getVisibility() == View.GONE) {
                chosenEntriesCounter++;

                //Since we're adding a new entry we're passing the old one to the second one,
                //since it became a second one now

                secondMergeEntry = firstMergeEntry;
                firstMergeEntry = entriesList.get(i);

                if(firstMergeEntry.isMerged && chosenEntriesCounter < 2){
                    animateUp(splitFab);
                }

                markedOrNot.setVisibility(View.VISIBLE);
                // if both are not null we can safely merge the 2 chosen
                if(secondMergeEntry != null){
                    animateUp(mergeFab);
                    wasMergedBefore = true;
                }
            }
            else{
                chosenEntriesCounter--;
                markedOrNot.setVisibility(View.GONE);
                if(firstMergeEntry.isMerged){
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

            return true;
        });*/
        mergeFab.setOnClickListener(view -> {
            if(chosenEntriesCounter > 2){
                chosenEntriesCounter--;
                Toast.makeText(rabbitActivity.this,R.string.alertMoreThan2Chosen,Toast.LENGTH_LONG).show();
                return;
            }
            secondMergeEntry.isMerged = true;
            secondMergeEntry.mergedEntryName = firstMergeEntry.entryName;
            secondMergeEntry.mergedEntry = firstMergeEntry;
            secondMergeEntry.mergedEntryPhLoc = firstMergeEntry.entryPhLoc;
            secondMergeEntry.update();


            firstMergeEntry.isChildMerged = true;
            firstMergeEntry.update();

            //reset and refresh the grid at the end
            animateDown(mergeFab);
            chosenEntriesCounter = 0;
            firstMergeEntry = null;
            secondMergeEntry = null;

            entriesList = fillData.getEntries(rabbitActivity.this,rabbitEntryView,this);
        });
        splitFab.setOnClickListener(view -> {
            firstMergeEntry.isMerged = false;
            firstMergeEntry.update();
            firstMergeEntry.mergedEntry.load();
            Entry secondMerge = firstMergeEntry.mergedEntry;
            secondMerge.isChildMerged = false;
            secondMerge.update();

            //reset and refresh the grid at the end
            animateDown(splitFab);
            chosenEntriesCounter =0;
            firstMergeEntry = null;
            secondMergeEntry = null;

            entriesList = fillData.getEntries(rabbitActivity.this,rabbitEntryView,this);
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
        if(requestCode == ADD_ENTRY_START && resultcode == RESULT_OK){
            entriesList = fillData.getEntries(this,rabbitEntryView,this);
            RecyclerView upcomingEvents = findViewById(R.id.upcomingAdapter);
            UpcomingEventsFragment.refreshFragment(upcomingEvents,this);
        }
        else if(requestCode == START_VIEW_ENTRY){
            entriesList = fillData.getEntries(this,rabbitEntryView,this);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent startViewEntry = new Intent(this,viewEntry.class);
        startViewEntry.putExtra("entryID",(UUID)view.getTag());
        startActivity(startViewEntry);
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

            if(firstMergeEntry.isMerged && chosenEntriesCounter < 2){
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
            if(firstMergeEntry.isMerged){
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
}
