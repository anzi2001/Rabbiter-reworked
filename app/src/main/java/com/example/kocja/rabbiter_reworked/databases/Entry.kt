package com.example.kocja.rabbiter_reworked.databases

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel

import java.util.Date
import java.util.UUID

/**
 * Created by kocja on 23/01/2018.
 */
@Table(database = appDatabase::class)
class Entry : BaseModel() {

    @PrimaryKey
    var entryID: UUID? = null
    @Column
    lateinit var entryName: String
    @Column
    lateinit var entryPhLoc: String
    @Column
    lateinit var matedWithOrParents: String
    @Column
    lateinit var secondParent: String
    @Column
    var birthDate: Date? = null
    @Column
    var matedDate: Date? = null
    @Column
    lateinit var chooseGender: String
    @Column
    var isMerged: Boolean = false
    @Column
    var isChildMerged: Boolean = false
    @Column
    lateinit var mergedEntryPhLoc: String
    @Column
    lateinit var mergedEntryName: String
    @ForeignKey(stubbedRelationship = true)
    var mergedEntry: Entry? = null
    @Column
    var rabbitNumber: Int = 0
    @Column
    var rabbitDeadNumber: Int = 0
}

