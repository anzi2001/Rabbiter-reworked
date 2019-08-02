package com.example.kocja.rabbiter_online.viewmodels

import android.net.Uri
import androidx.databinding.InverseMethod
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import java.util.*


class AddEntryViewModel constructor(private val fetcher : DataFetcher): ViewModel(){
    val entry : MutableLiveData<Entry> = MutableLiveData<Entry>().apply {value=Entry(UUID.randomUUID())}

    val birthDate : MutableLiveData<Date> by lazy{
        MutableLiveData<Date>()
    }
    val matedDate : MutableLiveData<Date> by lazy{
        MutableLiveData<Date>()
    }
    val lastDate : MutableLiveData<Date> by lazy{
        MutableLiveData<Date>()
    }
    val baseImageUri : MutableLiveData<Uri> by lazy{
        MutableLiveData<Uri>()
    }
    fun getAllEntries() : LiveData<List<Entry>>{
        return fetcher.getAllEntries()
    }
    fun setExistingEntryProperties(uuid : String) : LiveData<Entry>{
        return fetcher.seekSingleEntry(uuid)
    }
    fun updateEntry() : LiveData<String>{
        return fetcher.updateEntry(entry.value!!)
    }
    fun createNewEntry() : LiveData<String>{
        return fetcher.createNewEntry(entry.value!!)
    }



    fun createNewEvent(eventStr : String,eventDate : String,type : Int,uuid: UUID){
        val createEvent = Events()
        with(createEvent) {
            eventUUID = uuid
            name = entry.value?.entryName
            eventString = eventStr
            dateOfEvent = eventDate
            typeOfEvent = type
            numDead = entry.value!!.rabbitDeadNumber
            rabbitsNum = entry.value!!.rabbitNumber
        }

        fetcher.createNewEvent(createEvent)
    }
    fun findEventByUUID(uuid:String) : LiveData<Events>{
        return fetcher.findEventByUUID(uuid)
    }


}