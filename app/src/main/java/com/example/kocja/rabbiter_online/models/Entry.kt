package com.example.kocja.rabbiter_online.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 * Created by kocja on 23/01/2018.
 */
@Parcelize
data class Entry(val entryUUID: String,
                 var entryName: String = "",
                 var entryPhotoUri: String? = null,
                 var entryPhotoURL: String = "https://kocjancic.ddns.net/image/",
                 var matedWithOrParents: String? = null,
                 var secondParent: String? = null,
                 var birthDate: String? = null,
                 var matedDate: String? = null,
                 var chooseGender: String? = null,
                 var isMerged: Boolean = false,
                 var isChildMerged: Boolean = false,
                 var mergedEntryPhotoUri: String? = null,
                 var mergedEntryPhotoURL: String = "https://kocjancic.ddns.net/image/",
                 var mergedEntryName: String? = null,
                 var mergedEntryID: String? = null,
                 var rabbitNumber: Int = 0,
                 var rabbitDeadNumber: Int = 0) : Parcelable

