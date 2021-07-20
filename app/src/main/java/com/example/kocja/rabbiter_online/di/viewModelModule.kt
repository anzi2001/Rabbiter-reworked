package com.example.kocja.rabbiter_online.di

import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.viewmodels.AddEntryViewModel
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryStatsViewModel
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import okhttp3.OkHttpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

val viewModelModule = module{
    single{ getRetrofit()}
    single{ getWebService(get())}

    viewModel { AddEntryViewModel(get()) }
    viewModel { ViewEntryViewModel(get()) }
    viewModel { RabbitViewModel(get()) }
    viewModel { ViewEntryStatsViewModel(get()) }
}

fun getRetrofit() : Retrofit {
    val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60,TimeUnit.SECONDS)
            .build()
    return Retrofit.Builder()
            .baseUrl("https://kocjancic.ddns.net")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
}

fun getWebService(retrofit: Retrofit) : WebService = retrofit.create(WebService::class.java)