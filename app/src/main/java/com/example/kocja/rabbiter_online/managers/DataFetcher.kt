package com.example.kocja.rabbiter_online.managers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DataFetcher(private val webService: WebService){
    fun getAllEntries() : LiveData<List<Entry>>{
        val entries = MutableLiveData<List<Entry>>()
        webService.allEntries().enqueue(object : Callback<List<Entry>>{
            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {
                Log.v("WEBAPIERROR",t.localizedMessage)
                throw t
            }

            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                entries.value = response.body()
            }
        })
        return entries
    }

    fun seekSingleEntry(entryUUID : String) : LiveData<Entry>{
        val liveData = MutableLiveData<Entry>()
        webService.seekEntry(entryUUID).enqueue(object : Callback<Entry>{
            override fun onFailure(call: Call<Entry>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<Entry>, response: Response<Entry>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun updateEntry(entry: Entry) : LiveData<String>{
        val liveData = MutableLiveData<String>()
        webService.updateEntry(entry).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun createNewEntry(entry: Entry) : LiveData<String>{
        val liveData = MutableLiveData<String>()
        webService.newEntry(entry).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun createNewEvent(events: Events){
        webService.newEvent(events).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.body() == "OK"){
                    Log.v("Event","New event was succesfully posted")
                }
            }
        })
    }
    fun findEventByUUID(uuid : String) : LiveData<Events>{
        val liveData = MutableLiveData<Events>()
        webService.seekEventByUUID(uuid).enqueue(object : Callback<Events>{
            override fun onFailure(call: Call<Events>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<Events>, response: Response<Events>) {
                liveData.value = response.body()
            }
        })
        return liveData

    }
    fun findEntryByUUID(uuid : String) : LiveData<Entry>{
        val liveData = MutableLiveData<Entry>()
        webService.seekEntry(uuid).enqueue(object: Callback<Entry>{
            override fun onFailure(call: Call<Entry>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<Entry>, response: Response<Entry>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun findImage(imageName : String) : LiveData<Bitmap>{
        val liveData = MutableLiveData<Bitmap>()
        webService.searchImage(imageName).enqueue(object: Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val bytes = response.body()?.bytes()
                bytes?.let{
                    liveData.value = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                }

            }
        })
        return liveData
    }
    fun getEventsName(name : String) : LiveData<List<Events>>{
        val liveData = MutableLiveData<List<Events>>()
        webService.getEventByName(name).enqueue(object : Callback<List<Events>>{
            override fun onFailure(call: Call<List<Events>>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<List<Events>>, response: Response<List<Events>>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun deleteEvent(uuid : String){
        webService.deleteEvent(uuid).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.v("event deleted","success!")
            }
        })
    }
    fun deleteEntry(uuid : String) : LiveData<String>{
        val liveData = MutableLiveData<String>()
        webService.deleteEntry(uuid).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun findPastEvents(name : String) : LiveData<List<Events>>{
        val liveData = MutableLiveData<List<Events>>()
        webService.getPastEvents(name).enqueue(object : Callback<List<Events>>{
            override fun onFailure(call: Call<List<Events>>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<List<Events>>, response: Response<List<Events>>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun findParentOf(name: String) : LiveData<List<Entry>>{
        val liveData = MutableLiveData<List<Entry>>()
        webService.parentOf(name).enqueue(object : Callback<List<Entry>>{
            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun findNotAlertedEvents() : LiveData<List<Events>>{
        val liveData  = MutableLiveData<List<Events>>
        webService.findNotAlertedeEvents().enqueue(object : Callback<List<Events>>{
            override fun onFailure(call: Call<List<Events>>, t: Throwable) {
                throw t
            }

            override fun onResponse(call: Call<List<Events>>, response: Response<List<Events>>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
}