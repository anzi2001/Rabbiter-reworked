package com.example.kocja.rabbiter_online.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kocja.rabbiter_online.models.Entry

class RabbitViewModel : ViewModel(){
    val firstChosenEntry = MutableLiveData<Entry>()
    val secondChosenEntry = MutableLiveData<Entry>()
}