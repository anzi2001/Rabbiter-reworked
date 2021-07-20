package com.example.kocja.rabbiter_online.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class AddEntryViewModel(private val fetcher : WebService): ViewModel(){
    var entry : Entry? = null
    var matedDateChanged : Boolean = false
    var hasEntryPhotoChanged = false
    var photoUri : MutableLiveData<Uri> = MutableLiveData()
    var entryBitmap : Bitmap? = null
    private val imageUrlName = "https://kocjancic.ddns.net/image/"
    private val formatter = SimpleDateFormat("dd/MM/yyyy",Locale.getDefault())

    suspend fun getAllEntries() : List<Entry>{
        return fetcher.allEntries()
    }
    suspend fun updateEntry(fileName : String) : String{
        return suspendCoroutine {cont->
            viewModelScope.launch(Dispatchers.IO) {
                val result = fetcher.updateEntry(entry!!)
                if (hasEntryPhotoChanged) cont.resume(uploadImage(fileName, entryBitmap!!))
                else cont.resume(result)
            }
        }
    }

    suspend fun createNewEntry(fileName: String) : String{
        return suspendCoroutine {cont->
            viewModelScope.launch(Dispatchers.IO) {
                val result = fetcher.newEntry(entry!!)
                if (photoUri.value != null) cont.resume(uploadImage(fileName,entryBitmap!!))
                else cont.resume(result)
            }
        }
    }

    fun setUriSpecificValues(name : String){
        entry?.entryPhotoUri = photoUri.value.toString()
        entry?.entryPhotoURL = imageUrlName + name
    }



    fun createNewEvent(eventStr : String,eventDate : String,type : Int,uuid: UUID) : Events{
        val createEvent = Events(uuid.toString())
        with(createEvent) {
            name = entry?.entryName
            eventString = eventStr
            dateOfEvent = eventDate
            dateOfEventMilis = formatter.parse(eventDate).time
            typeOfEvent = type
            numDead = entry!!.rabbitDeadNumber
            rabbitsNum = entry!!.rabbitNumber
        }
        viewModelScope.launch(Dispatchers.IO) { fetcher.newEvent(createEvent) }

        return createEvent
    }
    suspend fun findEventByUUID(uuid:String) : Events{
        return fetcher.seekEventByUUID(uuid)
    }
    private suspend fun uploadImage(imageName : String,image : Bitmap) : String {
        val byteArray = ByteArrayOutputStream(image.height*image.width)
        image.compress(Bitmap.CompressFormat.JPEG,100,byteArray)
        val encodedImage = Base64.encodeToString(byteArray.toByteArray(), Base64.DEFAULT)

        return fetcher.uploadImage(imageName,encodedImage)
    }

}