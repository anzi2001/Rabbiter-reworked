package com.example.kocja.rabbiter_online.managers

import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface WebService{

    @GET("/entry/all")
    fun allEntries() : Call<List<Entry>>

    @POST("/entry/update")
    fun updateEntry(@Body entry : Entry) : Call<String>

    @POST("/entry/new")
    fun newEntry(@Body entry : Entry) : Call<String>

    @GET("/entry/{uuid}")
    fun seekEntry(@Path("uuid") uuid : String) : Call<Entry>

    @GET("/entry/parents/{name}")
    fun parentOf(@Path("name") name : String) : Call<List<Entry>>

    @GET("/entry/merged")
    fun childMergedEntries() : Call<List<Entry>>

    @DELETE("/entry/delete/{uuid}")
    fun deleteEntry(@Path("uuid") uuid : String) : Call<String>


    @GET("/event/{uuid}")
    fun seekEventByUUID(@Path("uuid") uuid: String) : Call<Events>

    @POST("/event/new")
    fun newEvent(@Body event: Events) : Call<String>

    @GET("/event/birth/{uuid}")
    fun getBirthEvent(@Path("uuid") uuid : String) : Call<Events>

    @DELETE("/event/delete/{uuid}")
    fun deleteEvent(@Path("uuid") uuid: String) : Call<String>

    @GET("/event/{name}")
    fun getEventByName(@Path("name") name:String) : Call<List<Events>>

    @GET("/events/past/{entryName}")
    fun getPastEvents(@Path("entryName") entryName : String) : Call<List<Events>>

    @GET("/events/alerted/not")
    fun findNotAlertedeEvents() : Call<List<Events>>

    @GET("/event/alerted/not/{uuid}")
    fun findNotAlertedEvent(@Path("uuid") uuid : String) : Call<Events>


    @GET("/image/{uri}")
    fun searchImage(@Path("uri") uri : String) : Call<ResponseBody>



}