package com.example.kocja.rabbiter_reworked;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.activities.settingsActivity;
import com.example.kocja.rabbiter_reworked.activities.viewEntry;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.fragments.UpcomingEventsFragment;
import com.example.kocja.rabbiter_reworked.services.alertIfNotAlertedService;

import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class rabbitActivity extends AppCompatActivity {
    private static final int ADD_ENTRY_START = 0;
    private static final int START_VIEW_ENTRY = 1;
    private static final int START_PERMISSION_REQUEST =2;
    private int chosenEntriesCounter = 0;
    private GridView mainGrid;
    private List<Entry> entriesList;
    private Entry firstMergeEntry = null;
    private Entry secondMergeEntry = null;

    private boolean wasMergedBefore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rabbit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},START_PERMISSION_REQUEST);

        }

        Intent checkAlarms = new Intent(this,alertIfNotAlertedService.class);
        startService(checkAlarms);

        FloatingActionButton addFab =  findViewById(R.id.addFab);
        FloatingActionButton mergeFab = findViewById(R.id.mergeFab);
        FloatingActionButton splitFab = findViewById(R.id.splitFab);
        addFab.setOnClickListener(view -> {
            Intent addEntryIntent = new Intent(this,addEntryActivity.class);
            startActivityForResult(addEntryIntent, ADD_ENTRY_START);
        });
        mainGrid = findViewById(R.id.mainGrid);
        entriesList = fillData.getEntries(this,mainGrid);
        mainGrid.setOnItemClickListener((adapterView, view, i, l) -> {
            Intent startViewEntry = new Intent(rabbitActivity.this,viewEntry.class);
            startViewEntry.putExtra("entryID",(UUID)view.getTag());
            startActivityForResult(startViewEntry,START_VIEW_ENTRY);
        });

        mainGrid.setOnItemLongClickListener((adapterView, view, i, l) -> {
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

            return true;
        });
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

            entriesList = fillData.getEntries(rabbitActivity.this,mainGrid);
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

            entriesList = fillData.getEntries(rabbitActivity.this,mainGrid);
        });
    }

    private static void animateDown(FloatingActionButton toMove){
        toMove.animate().translationY(200);
    }
    private static void animateUp(FloatingActionButton toMove){
        toMove.animate().translationY(-100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rabbit_acitivty,menu);
        return super.onCreateOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.settings){
            Intent startSettings = new Intent(this,settingsActivity.class);
            startActivity(startSettings);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode,int resultcode, Intent data){
        if(requestCode == ADD_ENTRY_START && resultcode == RESULT_OK){
            entriesList = fillData.getEntries(this,mainGrid);
            ListView upcomingEvents = findViewById(R.id.upcomingList);
            UpcomingEventsFragment.refreshFragment(upcomingEvents,this);
        }
        else if(requestCode == START_VIEW_ENTRY){
            entriesList = fillData.getEntries(this,mainGrid);
        }
    }
}
