package com.example.kocja.rabbiter_online.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.models.Entry
import java.text.SimpleDateFormat
import java.util.*

class ViewEntryViewModel(private val fetcher: WebService) : ViewModel(){
    val entry : MutableLiveData<Entry> = MutableLiveData<Entry>().apply { Entry(UUID.randomUUID().toString()) }
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())

    suspend fun findEntryByUUID(uuid : String) = fetcher.seekEntry(uuid)
    suspend fun findEventsName() = fetcher.getEventByName(entry.value?.entryName!!)
    suspend fun deleteEvent(uuid : String){
        fetcher.deleteEvent(uuid)
    }
    suspend fun deleteEntry() = fetcher.deleteEntry(entry.value?.entryUUID.toString())
    suspend fun findPastEvents(name : String) = fetcher.getPastEvents(name)
    suspend fun findParentOf(name : String) = fetcher.parentOf(name)

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