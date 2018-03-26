package com.example.kocja.rabbiter_reworked.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Entry_Table;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.databases.Events_Table;
import com.example.kocja.rabbiter_reworked.fragments.HistoryFragment;
import com.example.kocja.rabbiter_reworked.fragments.viewEntryData;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.UUID;

/**
 * Created by kocja on 27/01/2018.
 */

public class viewEntry extends AppCompatActivity {
    private Entry mainEntry;
    private viewEntryData mainEntryFragment;
    private UUID mainEntryUUID;
    private boolean dataChanged = false;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);
        Intent currentIntent = getIntent();
        mainEntryUUID =(UUID) currentIntent.getSerializableExtra("entryID");
        ImageView mainEntryView = findViewById(R.id.mainEntryView);
        mainEntryView.setOnClickListener(view -> {

        });
        SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.entryID.eq(mainEntryUUID))
                .async()
                .querySingleResultCallback((transaction, entry) -> {
                    mainEntry = entry;

                    mainEntryFragment = (viewEntryData) getSupportFragmentManager().findFragmentById(R.id.mainEntryFragment);

                    ListView historyView = findViewById(R.id.upcomingList);
                    if(entry.chooseGender.equals("Male")){
                        HistoryFragment.maleParentOf(this,entry.entryName,historyView);
                    }
                    else {
                        HistoryFragment.setPastEvents(this, entry.entryName,historyView);
                    }

                    mainEntryFragment.setData(entry);


                    Glide.with(viewEntry.this).load(entry.entryPhLoc).into(mainEntryView);

                    if(entry.isMerged){
                        ImageView mergedView = findViewById(R.id.mergedView);
                        mergedView.setVisibility(View.VISIBLE);
                        Glide.with(this).load(entry.mergedEntryPhLoc).into(mergedView);
                    } 
                }).execute();
    }
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == addEntryActivity.EDIT_EXISTING_ENTRY && resultCode == RESULT_OK){
            SQLite.select()
                    .from(Entry.class)
                    .where(Entry_Table.entryID.eq(mainEntryUUID))
                    .async()
                    .querySingleResultCallback((transaction, entry) -> mainEntryFragment.setData(entry)).execute();
            dataChanged = true;

        }
    }
    public void onBackPressed(){
        if(dataChanged){
            setResult(RESULT_OK);
            finish();
        }
        else{
            super.onBackPressed();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_rabbit,menu);
        if(!mainEntry.isMerged){
            MenuItem showMerged = menu.findItem(R.id.showMergedEntry);
            showMerged.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.editEntry){
            Intent startEditProc = new Intent(viewEntry.this,addEntryActivity.class);
            startEditProc.putExtra("getMode",addEntryActivity.EDIT_EXISTING_ENTRY);
            startEditProc.putExtra("entryEdit",mainEntry.entryID);
            startActivityForResult(startEditProc,addEntryActivity.EDIT_EXISTING_ENTRY);
        }
        else if(id == R.id.deleteEntry){
            AlertDialog.Builder assureDeletion = new AlertDialog.Builder(viewEntry.this)
                    .setTitle("Are you sure you want to delete?")
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                            SQLite.select()
                                    .from(Events.class)
                                    .where(Events_Table.name.eq(mainEntry.entryName))
                                    .async()
                                    .queryListResultCallback((transaction, tResult) -> {
                                        for(Events event: tResult){
                                            event.delete();
                                        }
                                        mainEntry.delete();
                                        setResult(RESULT_OK);
                                        finish();
                                    }).execute())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
            assureDeletion.show();
        }
        else if (id == R.id.showMergedEntry) {
            Intent startMergedViewEntry = new Intent(getApplicationContext(), viewEntry.class);
            startMergedViewEntry.putExtra("entryID", mainEntry.mergedEntry.entryID);
            startActivity(startMergedViewEntry);
        }
        else if(id == R.id.entryStats){
            Intent startStatActivity = new Intent(getApplicationContext(),viewEntryStats.class);
            startStatActivity.putExtra("entryUUID",mainEntry.entryID);
            startActivity(startStatActivity);
        }
        return super.onOptionsItemSelected(item);
    }
}
