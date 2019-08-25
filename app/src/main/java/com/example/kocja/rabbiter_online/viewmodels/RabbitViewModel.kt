package com.example.kocja.rabbiter_online.viewmodels

import android.util.SparseIntArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.extensions.notifyObserver
import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events

class RabbitViewModel(private val fetcher : DataFetcher) : ViewModel(){
    val entriesList : MutableLiveData<MutableList<Entry>> = MutableLiveData<MutableList<Entry>>().apply { value= mutableListOf() }
    val chosenPositions : SparseIntArray = SparseIntArray()

    fun onMergeClick(onDone : ()->Unit){
        val firstMergeEntry = entriesList.value!![chosenPositions.keyAt(0)]
        val secondMergeEntry = entriesList.value!![chosenPositions.keyAt(1)]

        with(firstMergeEntry){
            isMerged = true
            mergedEntryName = secondMergeEntry.entryName
            mergedEntryID = secondMergeEntry.entryUUID
            mergedEntryPhotoURL = secondMergeEntry.entryPhotoURL
        }
        entriesList.value!![chosenPositions.keyAt(0)] = firstMergeEntry
        fetcher.updateEntry(firstMergeEntry){
            secondMergeEntry.isChildMerged = true
            entriesList.value!![chosenPositions.keyAt(1)] = secondMergeEntry
            fetcher.updateEntry(secondMergeEntry){
                entriesList.value!!.removeAt(entriesList.value!!.map{it.entryUUID}.indexOf(secondMergeEntry.entryUUID))

                entriesList.notifyObserver()
                onDone()
            }
        }
        //getEntries()
    }
    fun onSplitClick(onDone: () -> Unit){
        val firstMergeEntry = entriesList.value!![chosenPositions.keyAt(0)]
        val secondMergeEntryUUID = firstMergeEntry.mergedEntryID
        firstMergeEntry.isMerged = false
        firstMergeEntry.mergedEntryName = null
        firstMergeEntry.mergedEntryID = null
        firstMergeEntry.mergedEntryPhotoURL = "https://kocjancic.ddns.net/image/"

        entriesList.value!![chosenPositions.keyAt(0)] = firstMergeEntry
        fetcher.updateEntry(firstMergeEntry)
        fetcher.findEntryByUUID(secondMergeEntryUUID!!){secondMergeEntry->
            secondMergeEntry?.isChildMerged = false
            fetcher.updateEntry(secondMergeEntry!!){
                entriesList.value!!.add(secondMergeEntry)
                chosenPositions.clear()
                entriesList.notifyObserver()
                onDone()
            }
        }
        //getEntries()
    }
    fun getEntries() {
        fetcher.findChildMergedEntries{
            entriesList.value?.clear()
            entriesList.value?.addAll(it)
            entriesList.notifyObserver()
        }
    }
    fun findNotAlertedEvents(onDone : (response : List<Events>)->Unit){
        fetcher.findNotAlertedEvents(onDone)
    }
}