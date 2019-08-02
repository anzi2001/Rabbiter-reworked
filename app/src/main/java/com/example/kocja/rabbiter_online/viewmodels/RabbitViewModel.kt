package com.example.kocja.rabbiter_online.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.models.Entry

class RabbitViewModel(private val fetcher : DataFetcher) : ViewModel(){
    private val firstChosenEntry = MutableLiveData<Entry>()
    private val secondChosenEntry = MutableLiveData<Entry>()

    fun updateEntry(which : Int){
        fetcher.updateEntry(if(which == 0){
            firstChosenEntry.value!!
        }else{
            secondChosenEntry.value!!
        })
    }
}