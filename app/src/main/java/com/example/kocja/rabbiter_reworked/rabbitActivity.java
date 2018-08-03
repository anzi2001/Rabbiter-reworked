package com.example.kocja.rabbiter_reworked;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.activities.addEntryActivity;
import com.example.kocja.rabbiter_reworked.activities.viewEntry;
import com.example.kocja.rabbiter_reworked.adapters.EntriesRecyclerAdapter;
import com.example.kocja.rabbiter_reworked.adapters.UpcomingEventsAdapter;
import com.example.kocja.rabbiter_reworked.databases.Entry;
import com.example.kocja.rabbiter_reworked.databases.Events;
import com.example.kocja.rabbiter_reworked.fragments.UpcomingEventsFragment;
import com.example.kocja.rabbiter_reworked.services.alertIfNotAlertedService;
import com.google.gson.Gson;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_rabbit_acitivty,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         if(item.getItemId() == R.id.moveOnline){
            MediaType json = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();
            SQLite.select()
                    .from(Entry.class)
                    .async()
                    .queryListResultCallback((transaction, tResult) -> {
                        for(Entry entry : tResult){
                            if(entry.entryPhLoc != null) {
                                File imgFile;
                                RequestBody reqBody;
                                String realPath = getRealPathContentUri(Uri.parse(entry.entryPhLoc));
                                imgFile = new File(realPath);
                                Log.d("imgFileName",imgFile.getName());
                                Log.d("uri path",Uri.parse(entry.entryPhLoc).getPath().substring(11));
                                MultipartBody.Builder multipartBody = new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM)
                                        .addFormDataPart("entry", gson.toJson(entry))
                                        .addFormDataPart("entryImage", imgFile.getName(), RequestBody.create(MediaType.parse("image/jpg"), imgFile));
                                if(entry.mergedEntryPhLoc != null){
                                    entry.mergedEntryPhLoc = Uri.parse(entry.mergedEntryPhLoc).getPath().substring(11);
                                }
                                reqBody = multipartBody.build();
                                Request req = new Request.Builder()
                                        .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/moveOnlineEntry")
                                        .post(reqBody)
                                        .build();
                                client.newCall(req).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) {

                                    }
                                });
                            }
                            else{
                                Request req = new Request.Builder()
                                        .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/moveOnlineEntryNoFile")
                                        .post(RequestBody.create(json,gson.toJson(entry)))
                                        .build();
                                client.newCall(req).enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response){
                                    }
                                });
                            }
                        }
                    }).execute();

            SQLite.select()
                    .from(Events.class)
                    .async()
                    .queryListResultCallback((transaction, tResult) -> {
                        for(Events event : tResult){
                            Request req = new Request.Builder()
                                    .url("http://nodejs-mongo-persistent-rabbit.a3c1.starter-us-west-1.openshiftapps.com/moveOnlineEvent")
                                    .post(RequestBody.create(json,gson.toJson(event)))
                                    .build();
                            client.newCall(req).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                }
                            });
                        }
                    }).execute();
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode,int resultcode, Intent data){
        super.onActivityResult(requestCode,resultcode,data);
        if(requestCode == ADD_ENTRY_START && resultcode == RESULT_OK){
            entriesList = fillData.getEntries(this,rabbitEntryView,this);
            RecyclerView upcomingEvents = findViewById(R.id.upcomingAdapter);
            UpcomingEventsFragment.refreshFragment(upcomingEvents,this);
            UpcomingEventsFragment.updateNotesToDisplay();
        }
        else if(requestCode == START_VIEW_ENTRY){
            entriesList = fillData.getEntries(this,rabbitEntryView,this);
            UpcomingEventsFragment.updateNotesToDisplay();
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
    private String getRealPathContentUri(Uri contentUri){
        String images[] = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri,images,null,null,null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(images[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }
}
