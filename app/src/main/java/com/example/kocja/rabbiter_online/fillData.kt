package com.example.kocja.rabbiter_online


import android.app.Activity
import android.graphics.BitmapFactory
import android.util.Log

import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_online.databases.Entry
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager

import java.util.ArrayList
import java.util.Arrays

import androidx.recyclerview.widget.RecyclerView


/**
 * Created by kocja on 05/02/2018.
 */

internal object fillData {
    private var temporaryList: List<Entry> = ArrayList(0)
    fun getEntries(context: Activity, view: RecyclerView, listener: EntriesRecyclerAdapter.onItemClickListener, post: onPost) {
        HttpManager.getRequest("seekChildMergedEntries") { response ->
            Log.v("seekChildMergedEntries", response)
            val multiples = GsonManager.getGson()!!.fromJson<Array<Entry>>(response, Array<Entry>::class.java)
            temporaryList = Arrays.asList(*multiples)
            for (entry in temporaryList) {
                HttpManager.postRequest("searchForImage", GsonManager.getGson()!!.toJson(entry.entryPhLoc)) { _ ,bytes->
                    entry.entryBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                    if (entry.isMerged) {
                        HttpManager.postRequest("searchForImage", GsonManager.getGson()!!.toJson(entry.mergedEntryPhLoc)) {  _, bytes1 -> entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1, 0, bytes1!!.size) }
                    }
                }
            }
            HttpManager.handler.post {
                val adapter = EntriesRecyclerAdapter(context, temporaryList)
                adapter.setLongClickListener(listener)
                view.adapter = adapter
                post.onPostProcess(temporaryList)
            }
        }
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

    interface onPost {
        fun onPostProcess(temporaryList: List<Entry>)
    }


}
