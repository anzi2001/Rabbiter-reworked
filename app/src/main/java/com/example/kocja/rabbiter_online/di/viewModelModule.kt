package com.example.kocja.rabbiter_online.di

import com.example.kocja.rabbiter_online.managers.DataFetcher
import com.example.kocja.rabbiter_online.managers.WebService
import com.example.kocja.rabbiter_online.viewmodels.AddEntryViewModel
import com.example.kocja.rabbiter_online.viewmodels.RabbitViewModel
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryStatsViewModel
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

val viewModelModule = module{
    single{ getRetrofit()}
    single{ getWebService(get())}
    single{ DataFetcher(get()) }

    viewModel { AddEntryViewModel(get()) }
    viewModel { ViewEntryViewModel(get()) }
    viewModel { RabbitViewModel(get()) }
    viewModel { ViewEntryStatsViewModel(get()) }
}

fun getRetrofit() : Retrofit {
    val httpLoggingInterceptor = HttpLoggingInterceptor()
    val interceptor = httpLoggingInterceptor.apply { httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS }
    val client = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60,TimeUnit.SECONDS)
            .addInterceptor(interceptor).build()
    return Retrofit.Builder()
            .baseUrl("https://kocjancic.ddns.net")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
}

fun getWebService(retrofit: Retrofit) : WebService = retrofit.create(WebService::class.java)