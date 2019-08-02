package com.example.kocja.rabbiter_online


import android.app.Activity
import android.graphics.BitmapFactory

import com.example.kocja.rabbiter_online.adapters.EntriesRecyclerAdapter
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager


import androidx.recyclerview.widget.RecyclerView


/**
 * Created by kocja on 05/02/2018.
 */

internal object FillData {
    fun getEntries(context: Activity, view: RecyclerView,activity: RabbitActivity) {
        HttpManager.getRequest("seekChildMergedEntries") { response ->
            val temporaryList = GsonManager.gson.fromJson(response, Array<Entry>::class.java).toList()

            for (entry in temporaryList) {
                HttpManager.postRequest("searchForImage", GsonManager.gson.toJson(entry.entryPhLoc)) { _ ,bytes->
                    entry.entryBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                    if (entry.isMerged) {
                        HttpManager.postRequest("searchForImage", GsonManager.gson.toJson(entry.mergedEntryPhLoc)) {  _, bytes1 -> entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1, 0, bytes1!!.size) }
                    }
                }
            }
            HttpManager.handler.post {
                val adapter = EntriesRecyclerAdapter(context, temporaryList)
                adapter.setLongClickListener(activity)
                view.adapter = adapter
                activity.onPostProcess(temporaryList)
            }
        }
    }

    interface OnPost {
        fun onPostProcess(temporaryList: List<Entry>)
    }


}
