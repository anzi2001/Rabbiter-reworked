package com.example.kocja.rabbiter_online;


import android.app.Activity;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter;
import com.example.kocja.rabbiter_online.databases.Entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * Created by kocja on 05/02/2018.
 */

class fillData {
    private static List<Entry> temporaryList = new ArrayList<>(0);
    static void getEntries(Activity context, RecyclerView view,EntriesRecyclerAdapter.onItemClickListener listener,onPost post){
        HttpManager.getRequest("seekChildMergedEntries", response -> {
            Log.v("seekChildMergedEntries",response);
            Entry[] multiples = GsonManager.getGson().fromJson(response, Entry[].class);
            temporaryList = Arrays.asList(multiples);
            for(Entry entry : temporaryList){
                HttpManager.postRequest("searchForImage", GsonManager.getGson().toJson(entry.entryPhLoc), (response1, bytes) -> {
                    entry.entryBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    if(entry.isMerged){
                        HttpManager.postRequest("searchForImage", GsonManager.getGson().toJson(entry.mergedEntryPhLoc), (response2, bytes1) -> entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1,0,bytes1.length));
                    }
                });
            }
            HttpManager.handler.post(() -> {
                EntriesRecyclerAdapter adapter = new EntriesRecyclerAdapter(context,temporaryList);
                adapter.setLongClickListener(listener);
                view.setAdapter(adapter);
                post.onPostProcess(temporaryList);
            });
        });
        /*SQLite.select()
                .from(Entry.class)
                .where(Entry_Table.isChildMerged.eq(false))
                .async()
                .queryListResultCallback((transaction, tResult) -> {
                    EntriesRecyclerAdapter adapter = new EntriesRecyclerAdapter(context,tResult);
                    adapter.setLongClickListener(listener);
                    temporaryList.addAll(tResult);
                    view.setAdapter(adapter);
                }).execute();
        */
    }
    public interface onPost{
        void onPostProcess(List<Entry> temporaryList);
    }


}
