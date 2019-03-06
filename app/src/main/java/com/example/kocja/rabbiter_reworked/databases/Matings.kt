package com.example.kocja.rabbiter_reworked.databases

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import java.util.*

@Table(database = appDatabase::class)
class Matings : BaseModel(){
    @PrimaryKey
    var matingUUID : UUID? = null
    @Column
    var entryID : UUID? = null
    @Column
    var matingDate : Date? = null
}