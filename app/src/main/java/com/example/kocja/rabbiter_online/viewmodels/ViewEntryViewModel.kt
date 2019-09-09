package com.example.kocja.rabbiter_online.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import java.text.SimpleDateFormat
import java.util.*

class ViewEntryViewModel(private val fetcher: DataFetcher) : ViewModel(){
    val entry : MutableLiveData<Entry> = MutableLiveData<Entry>().apply { Entry(UUID.randomUUID().toString()) }
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())

    fun findEntryByUUID(uuid : String) : LiveData<Entry> {
        return fetcher.findEntryByUUID(uuid)
    }
    fun findEventsName() : LiveData<List<Events>>{
        return fetcher.getEventsName(entry.value?.entryName!!)
    }
    fun deleteEvent(uuid : String){
        fetcher.deleteEvent(uuid)
    }
    fun deleteEntry() : LiveData<String>{
        return fetcher.deleteEntry(entry.value?.entryUUID.toString())
    }
    fun findPastEvents(name : String) : LiveData<List<Events>>{
        return fetcher.findPastEvents(name)
    }
    fun findParentOf(name : String) : LiveData<List<Entry>>{
        return fetcher.findParentOf(name)
    }
    fun calculateRabbitAge() : String{
        return if(entry.value?.birthDate!!.isNotEmpty()){
            val birthDate = simpleDateFormat.parse(entry.value?.birthDate)
            val diffDate = Date().time - birthDate.time
            simpleDateFormat.format(Date(diffDate))
        }else{
            ""
        }
    }
}