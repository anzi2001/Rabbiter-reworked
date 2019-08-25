package com.example.kocja.rabbiter_online.extensions

import androidx.lifecycle.MutableLiveData

fun<T> MutableLiveData<T>.notifyObserver(){
    value = value
}