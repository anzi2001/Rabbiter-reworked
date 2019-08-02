package com.example.kocja.rabbiter_online.models

import android.graphics.Bitmap

import java.util.UUID

/**
 * Created by kocja on 23/01/2018.
 */
data class Entry(val entryID: UUID,
                 var entryName: String? = null,
                 var entryPhLoc: String? = null,
                 var entryBitmap: Bitmap? = null,
                 var matedWithOrParents: String? = null,
                 var secondParent: String? = null,
                 var birthDate: String? = null,
                 var chooseGender: String? = null,
                 var isMerged: Boolean = false,
                 var isChildMerged: Boolean = false,
                 var mergedEntryPhLoc: String? = null,
                 var mergedEntryBitmap: Bitmap? = null,
                 var mergedEntryName: String? = null,
                 var mergedEntry: String? = null,
                 var rabbitNumber: Int = 0,
                 var rabbitDeadNumber: Int = 0){

    var matedDate: String? = null
        set(value){
            matedDateChanged = true
            field = value
        }

    @Transient
    var matedDateChanged = false
}

