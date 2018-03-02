package com.example.kocja.rabbiter_reworked;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.activities.viewEntry;
import com.example.kocja.rabbiter_reworked.databases.Entry;
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
                Toast.makeText(rabbitActivity.this,"You have more than 2 entries chosen which is not possible",Toast.LENGTH_LONG).show();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rabbit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode,int resultcode, Intent data){
        if((requestCode == ADD_ENTRY_START ||requestCode == START_VIEW_ENTRY) && resultcode == RESULT_OK){
            entriesList = fillData.getEntries(this,mainGrid);
        }
    }
}
