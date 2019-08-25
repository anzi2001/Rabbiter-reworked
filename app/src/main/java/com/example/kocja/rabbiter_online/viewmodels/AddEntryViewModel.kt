package com.example.kocja.rabbiter_online.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import java.text.SimpleDateFormat
import java.util.*


class AddEntryViewModel(private val fetcher : DataFetcher): ViewModel(){
    val entry : MutableLiveData<Entry> = MutableLiveData()
    var matedDateChanged : Boolean = false
    var hasEntryPhotoChanged = false
    var photoUri : MutableLiveData<Uri> = MutableLiveData()
    var entryBitmap : Bitmap? = null
    private val imageUrlName = "https://kocjancic.ddns.net/image/"
    private val formatter = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())

    fun getAllEntries() : LiveData<List<Entry>>{
        return fetcher.getAllEntries()
    }
    fun updateEntry() : LiveData<String>{
        return fetcher.updateEntry(entry.value!!)
    }
    fun createNewEntry() : LiveData<String>{
        return fetcher.createNewEntry(entry.value!!)
    }

    fun setUriSpecificValues(name : String){
        entry.value!!.entryPhotoUri = photoUri.value.toString()
        entry.value!!.entryPhotoURL = imageUrlName + name
    }

    fun createNewEvent(eventStr : String,eventDate : String,type : Int,uuid: UUID) : Events{
        val createEvent = Events(uuid.toString())
        with(createEvent) {
            name = entry.value?.entryName
            eventString = eventStr
            dateOfEvent = eventDate
            dateOfEventMilis = formatter.parse(eventDate).time
            typeOfEvent = type
            numDead = entry.value!!.rabbitDeadNumber
            rabbitsNum = entry.value!!.rabbitNumber
        }

        fetcher.createNewEvent(createEvent)
        return createEvent
    }
    fun findEventByUUID(uuid:String) : LiveData<Events>{
        return fetcher.findEventByUUID(uuid)
    }
    fun uploadImage(imageName : String,image : Bitmap,onDone : (response : String?)->Unit){
        return fetcher.uploadImage(imageName,image,onDone)
    }

}