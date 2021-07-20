package com.example.kocja.rabbiter_online.viewmodels

import android.util.SparseIntArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kocja.rabbiter_online.extensions.notifyObserver
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RabbitViewModel(private val fetcher : WebService) : ViewModel(){
    var entriesList : MutableList<Entry> = mutableListOf()
    val chosenPositions : SparseIntArray = SparseIntArray()

    fun onMergeClick(){
        val firstMergeEntry = entriesList[chosenPositions.keyAt(0)]
        val secondMergeEntry = entriesList[chosenPositions.keyAt(1)]

        with(firstMergeEntry){
            isMerged = true
            mergedEntryName = secondMergeEntry.entryName
            mergedEntryID = secondMergeEntry.entryUUID
            mergedEntryPhotoURL = secondMergeEntry.entryPhotoURL
        }
        entriesList[chosenPositions.keyAt(0)] = firstMergeEntry
        viewModelScope.launch {
            fetcher.updateEntry(firstMergeEntry)
            secondMergeEntry.isChildMerged = true
            fetcher.updateEntry(secondMergeEntry)
            withContext(Dispatchers.Main){
                entriesList[chosenPositions.keyAt(1)] = secondMergeEntry
                entriesList.removeAt(entriesList.indexOfFirst {it.entryUUID == secondMergeEntry.entryUUID })
            }

        }
    }
    suspend fun onSplitClick(){
        val firstMergeEntry = entriesList[chosenPositions.keyAt(0)]
        val secondMergeEntryUUID = firstMergeEntry.mergedEntryID
        with(firstMergeEntry){
            isMerged = false
            mergedEntryName = null
            mergedEntryID = null
            mergedEntryPhotoURL = "https://kocjancic.ddns.net/image/"
        }

        entriesList[chosenPositions.keyAt(0)] = firstMergeEntry
        fetcher.updateEntry(firstMergeEntry)

        val secondMergeEntry = fetcher.seekEntry(secondMergeEntryUUID!!)
        secondMergeEntry.isChildMerged = false
        fetcher.updateEntry(secondMergeEntry)

        entriesList.add(secondMergeEntry)
        chosenPositions.clear()
        //entriesList.notifyObserver()
    }

    fun getEntries() {
        viewModelScope.launch{
            val result = fetcher.childMergedEntries()
            withContext(Dispatchers.Main){
                entriesList.clear()
                entriesList.addAll(result)
                //entriesList.notifyObserver()
            }

        }
    }
    suspend fun findNotAlertedEvents() : List<Events> = fetcher.findNotAlertedEvents()
}