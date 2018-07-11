package com.example.kocja.rabbiter_online.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_online.managers.GsonManager;
import com.example.kocja.rabbiter_online.managers.HttpManager;
import com.example.kocja.rabbiter_online.R;
import com.example.kocja.rabbiter_online.databases.Entry;
import com.example.kocja.rabbiter_online.databases.Events;
import com.example.kocja.rabbiter_online.fragments.HistoryFragment;
import com.example.kocja.rabbiter_online.fragments.viewEntryData;
import com.google.gson.Gson;

import java.util.UUID;


/**
 * Created by kocja on 27/01/2018.
 */

public class viewEntry extends AppCompatActivity {
    private Entry mainEntry;
    private viewEntryData mainEntryFragment;
    private boolean dataChanged = false;
    private ImageView mainEntryView;
    private Gson gson;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entry);
        Intent currentIntent = getIntent();

        gson = GsonManager.getGson();
        UUID mainEntryUUID = (UUID) currentIntent.getSerializableExtra("entryID");
        mainEntryView = findViewById(R.id.mainEntryView);
        Intent viewLargerImage = new Intent(this,largerMainImage.class);
        mainEntryView.setOnClickListener(view -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                mainEntryView.setTransitionName("closerLook");
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,mainEntryView,"closerLook");
                startActivity(viewLargerImage,options.toBundle());
            }
            else{
                startActivity(viewLargerImage);
            }

        });
        HttpManager.postRequest("seekSingleEntry",gson.toJson(mainEntryUUID), (response, bytes) -> {

            this.runOnUiThread(() -> {
                Entry entry = gson.fromJson(response,Entry[].class)[0];
                viewLargerImage.putExtra("imageURI", entry.getEntryPhLoc());
                mainEntry = entry;
                mainEntryFragment = (viewEntryData) getSupportFragmentManager().findFragmentById(R.id.mainEntryFragment);

                RecyclerView historyView = findViewById(R.id.upcomingAdapter);

                if(entry.getChooseGender().equals(getString(R.string.genderMale))){
                    HistoryFragment.maleParentOf(this, entry.getEntryName(),historyView,viewEntry.this);
                }
                else {
                    HistoryFragment.setPastEvents(this,entry.getEntryName(),historyView);
                }

                mainEntryFragment.setData(entry);

                HttpManager.postRequest("searchForImage", gson.toJson(entry.getEntryPhLoc()), (response1, bytes1) -> {
                    entry.setEntryBitmap(BitmapFactory.decodeByteArray(bytes1,0, bytes1.length));
                    this.runOnUiThread(() -> Glide.with(viewEntry.this).load(entry.getEntryBitmap()).into(mainEntryView));

                });


                if(entry.isMerged()){
                    ImageView mergedView = findViewById(R.id.mergedView);
                    mergedView.setOnClickListener(view -> {
                        Intent startMergedMain = new Intent(this,viewEntry.class);
                        startMergedMain.putExtra("entryID",entry.getMergedEntry());
                        ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation(viewEntry.this,mainEntryView,"mergedName");
                        startActivity(startMergedMain,compat.toBundle());
                    });
                    mergedView.setVisibility(View.VISIBLE);
                    HttpManager.postRequest("searchForImage", gson.toJson(entry.getMergedEntryPhLoc()), (response1, bytes1) -> {
                        entry.setMergedEntryBitmap(BitmapFactory.decodeByteArray(bytes1,0, bytes1.length));
                        this.runOnUiThread(()->Glide.with(viewEntry.this).load(entry.getMergedEntryBitmap()).into(mergedView));
                    });

                }
            });

        });
    }
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(requestCode == addEntryActivity.EDIT_EXISTING_ENTRY && resultCode == RESULT_OK){
            HttpManager.postRequest("seekSingleEntry", gson.toJson(mainEntry), (response,bytes) -> {
                Entry entry = gson.fromJson(response,Entry[].class)[0];
                this.runOnUiThread(() -> {
                    mainEntryFragment.setData(entry);
                    Glide.with(viewEntry.this)
                            .load(entry.getEntryPhLoc())
                            .into(mainEntryView);
                });

            });
            dataChanged = true;
        }
    }
    public void onBackPressed(){
        if(dataChanged){
            setResult(RESULT_OK);
            supportFinishAfterTransition();
            //finish();
        }
        else{
            super.onBackPressed();
        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_view_entry_data,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.editEntry) {
            Intent startEditProc = new Intent(viewEntry.this, addEntryActivity.class);
            startEditProc.putExtra("getMode", addEntryActivity.EDIT_EXISTING_ENTRY);
            startEditProc.putExtra("entryEdit", mainEntry.getEntryID());
            startActivityForResult(startEditProc, addEntryActivity.EDIT_EXISTING_ENTRY);

        } else if (id == R.id.deleteEntry) {
            AlertDialog.Builder assureDeletion = new AlertDialog.Builder(viewEntry.this)
                    .setTitle(R.string.confirmDeletion)
                    .setPositiveButton(R.string.confirm, (dialogInterface, i) -> HttpManager.postRequest("seekEventsName", gson.toJson(mainEntry.getEntryName()), (response, bytes) -> {
                        Events[] events = gson.fromJson(response,Events[].class);
                        this.runOnUiThread(() -> {
                            for (Events event : events) {
                                HttpManager.postRequest("deleteEvent", gson.toJson(event.getEventUUID()), (response1,bytes1) -> {
                                });
                            }
                            HttpManager.postRequest("deleteEntry", gson.toJson(mainEntry.getEntryID()), (response1,bytes1) -> {
                                setResult(RESULT_OK);
                                finish();
                            });
                        });


                    }))
                    .setNegativeButton(R.string.decline, (dialogInterface, i) -> dialogInterface.cancel());
            assureDeletion.show();

        }
        else if (id == R.id.entryStats) {
            Intent startStatActivity = new Intent(getApplicationContext(), viewEntryStats.class);
            startStatActivity.putExtra("entryUUID", mainEntry.getEntryID());
            startActivity(startStatActivity);

        }
        else if(id == R.id.showMerged){
            if(mainEntry.isMerged()){
                Intent showMerged = new Intent(getApplicationContext(),viewEntry.class);
                showMerged.putExtra("entryID",UUID.fromString(mainEntry.getMergedEntry()));
                startActivity(showMerged);
            }
            else{
                AlertDialog.Builder alertNotMerged = new AlertDialog.Builder(this)
                        .setTitle("Oops")
                        .setMessage("your entry is not merged")
                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.cancel());
                alertNotMerged.show();
            }

        }
        return super.onOptionsItemSelected(item);
    }
}
