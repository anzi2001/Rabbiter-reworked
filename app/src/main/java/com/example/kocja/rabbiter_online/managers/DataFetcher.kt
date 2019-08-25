package com.example.kocja.rabbiter_online.managers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class DataFetcher(private val webService: WebService){
    fun getAllEntries() : LiveData<List<Entry>>{
        val entries = MutableLiveData<List<Entry>>()
        webService.allEntries().enqueue(object : Callback<List<Entry>>{
            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {
                Log.v("WEBAPIERROR","getAllEntries")
                throw t
            }

            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                entries.value = response.body()
            }
        })
        return entries
    }
    fun updateEntry(entry: Entry) : LiveData<String>{
        val liveData = MutableLiveData<String>()
        webService.updateEntry(entry).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.v("WEBAPIERROR","updateEntry")
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun updateEntry(entry: Entry,onDone: (response : String?)->Unit){
        webService.updateEntry(entry).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.v("WEBAPIERROR","updateEntry")
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                onDone(response.body())
            }
        })
    }
    fun createNewEntry(entry: Entry) : LiveData<String>{
        val liveData = MutableLiveData<String>()
        webService.newEntry(entry).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.v("WEBAPIERROR","createNewEntry")
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
                Log.v("WEBAPIERROR","createNewEntry")
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
                Log.v("WEBAPIERROR","findEventByUUID")
                throw t
            }

            override fun onResponse(call: Call<Events>, response: Response<Events>) {
                liveData.value = response.body()
            }
        })
        return liveData

    }
    fun findEventByUUID(uuid : String,onDone:(event:Events)->Unit){
        webService.seekEventByUUID(uuid).enqueue(object : Callback<Events>{
            override fun onFailure(call: Call<Events>, t: Throwable) {
                Log.v("WEBAPIERROR","findEventByUUID")
                throw t
            }

            override fun onResponse(call: Call<Events>, response: Response<Events>) {
                onDone(response.body()!!)
            }
        })
    }
    fun findEntryByUUID(uuid : String) : LiveData<Entry>{
        val liveData = MutableLiveData<Entry>()
        webService.seekEntry(uuid).enqueue(object: Callback<Entry>{
            override fun onFailure(call: Call<Entry>, t: Throwable) {
                Log.v("WEBAPIERROR","findEntryByUUID")
                throw t
            }

            override fun onResponse(call: Call<Entry>, response: Response<Entry>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun findEntryByUUID(uuid : String, onDone : (response : Entry?)->Unit){
        webService.seekEntry(uuid).enqueue(object: Callback<Entry>{
            override fun onFailure(call: Call<Entry>, t: Throwable) {
                Log.v("WEBAPIERROR","findEntryByUUID")
                throw t
            }

            override fun onResponse(call: Call<Entry>, response: Response<Entry>) {
                onDone(response.body())
            }
        })
    }
    /*fun findImage(imageName : String?) : LiveData<Bitmap>{
        val liveData = MutableLiveData<Bitmap>()
        webService.searchImage(imageName.orEmpty()).enqueue(object: Callback<ResponseBody>{
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.v("WEBAPIERROR","findImage")
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
    }*/
    fun getEventsName(name : String) : LiveData<List<Events>>{
        val liveData = MutableLiveData<List<Events>>()
        webService.getEventByName(name).enqueue(object : Callback<List<Events>>{
            override fun onFailure(call: Call<List<Events>>, t: Throwable) {
                Log.v("WEBAPIERROR","getEventsName")
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
                Log.v("WEBAPIERROR","deleteEvent")
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
                Log.v("WEBAPIERROR","deleteEntry")
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
                Log.v("WEBAPIERROR","findPastEvents")
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
                Log.v("WEBAPIERROR","findParentOf")
                throw t
            }

            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                liveData.value = response.body()
            }
        })
        return liveData
    }
    fun findNotAlertedEvents(onDone : (response : List<Events>)->Unit) {
        webService.findNotAlertedEvents().enqueue(object : Callback<List<Events>>{
            override fun onFailure(call: Call<List<Events>>, t: Throwable) {
                Log.v("WEBAPIERROR","findNotAlertedEvents")
                throw t
            }

            override fun onResponse(call: Call<List<Events>>, response: Response<List<Events>>) {
                onDone(response.body()!!)
            }
        })
    }
    fun findChildMergedEntries(onDone : (response : List<Entry>)->Unit){
        webService.childMergedEntries().enqueue(object : Callback<List<Entry>>{
            override fun onFailure(call: Call<List<Entry>>, t: Throwable) {
                Log.v("WEBAPIERROR","findChildMergedEntries")
                throw t
            }

            override fun onResponse(call: Call<List<Entry>>, response: Response<List<Entry>>) {
                val body = response.body()
                if(body !== null){
                    onDone(body)
                }
                else{
                    onDone(emptyList())
                }

            }
        })
    }
    fun uploadImage(imageName : String, image : Bitmap,onDone: (response: String?) -> Unit){
        val byteArray = ByteArrayOutputStream(image.height*image.width)
        image.compress(Bitmap.CompressFormat.JPEG,100,byteArray)
        val encodedImage = Base64.encodeToString(byteArray.toByteArray(),Base64.DEFAULT)
        webService.uploadImage(imageName,encodedImage).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.v("WEBAPIERROR",t.localizedMessage)
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                onDone(response.body())
            }
        })
    }
    fun findEventsByNameType(name : String,onDone : (response: List<Events>)->Unit){
        webService.getEventByName(name).enqueue(object : Callback<List<Events>>{
            override fun onFailure(call: Call<List<Events>>, t: Throwable) {
                Log.v("WEBAPIERROR","findEventsByNameType")
                throw t
            }

            override fun onResponse(call: Call<List<Events>>, response: Response<List<Events>>) {
                onDone(response.body()!!)
            }
        })
    }
    fun updateEvent(event : Events,onDone :()->Unit){
        webService.updateEvent(event).enqueue(object : Callback<String>{
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.v("WEBAPIERROR","updateEvent")
                throw t
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                if(response.body() == "OK"){
                    Log.v("success","updated event sucessfully")
                }
                onDone()
            }
        })
    }
}