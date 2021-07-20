package com.example.kocja.rabbiter_online.managers

import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.models.Events
import okhttp3.ResponseBody
import retrofit2.http.*

interface WebService{

    @GET("/entry/all")
    suspend fun allEntries() : List<Entry>

    @POST("/entry/update")
    suspend fun updateEntry(@Body entry : Entry) : String

    @POST("/entry/new")
    suspend fun newEntry(@Body entry : Entry) : String

    @GET("/entry/{uuid}")
    suspend fun seekEntry(@Path("uuid") uuid : String) : Entry

    @GET("/entry/parents/{name}")
    suspend fun parentOf(@Path("name") name : String) : List<Entry>

    @GET("/entry/merged")
    suspend fun childMergedEntries() : List<Entry>

    @DELETE("/entry/delete/{uuid}")
    suspend fun deleteEntry(@Path("uuid") uuid : String) : String


    @GET("/event/{uuid}")
    suspend fun seekEventByUUID(@Path("uuid") uuid: String) : Events

    @POST("/event/new")
    suspend fun newEvent(@Body event: Events) : String

    @POST("/event/update")
    suspend fun updateEvent(@Body event: Events) : String


    @DELETE("/event/delete/{uuid}")
    suspend fun deleteEvent(@Path("uuid") uuid: String) : String

    @GET("/event/name/{name}")
    suspend fun getEventByName(@Path("name") name:String) : List<Events>

    @GET("/events/past/{entryName}")
    suspend fun getPastEvents(@Path("entryName") entryName : String) : List<Events>

    @GET("/events/alerted/not")
    suspend fun findNotAlertedEvents() : List<Events>

    @GET
    suspend fun searchImage(@Url uri : String) : ResponseBody
    @POST("/image/upload/{name}")
    suspend fun uploadImage(@Path("name") name : String, @Body base64EncodedImage : String) : String



}