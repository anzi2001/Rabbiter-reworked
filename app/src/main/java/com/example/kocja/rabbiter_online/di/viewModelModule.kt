package com.example.kocja.rabbiter_online.di

import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.viewmodels.AddEntryViewModel
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val viewModelModule = module{
    single{ getRetrofit()}
    single{ getWebService(get())}
    single{ DataFetcher(get()) }

    viewModel { AddEntryViewModel(get()) }
    viewModel { ViewEntryViewModel(get()) }
    viewModel { RabbitViewModel(get()) }
}

fun getRetrofit() : Retrofit {
    return Retrofit.Builder()
            .baseUrl("https://kocjancic.ddns.net")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

fun getWebService(retrofit: Retrofit) : WebService = retrofit.create(WebService::class.java)