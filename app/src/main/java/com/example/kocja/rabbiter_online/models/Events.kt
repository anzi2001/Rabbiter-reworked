package com.example.kocja.rabbiter_online.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by kocja on 28/01/2018.
 */
@Parcelize
class Events(val eventUUID: String,
             var name: String? = null,
             var secondParent: String? = null,
             var eventString: String? = null,
             var dateOfEvent: String? = null,
             var dateOfEventMilis: Long = 0,
             var rabbitsNum: Int = 0,
             var numDead: Int = 0,
             var id: Int = 0,
             var typeOfEvent: Int = 0,
             var timesNotified: Int = 0,
             var notificationState: Int = 0) : Parcelable {


    companion object {
        const val EVENT_FAILED = -1
        const val NOT_YET_ALERTED = 0
        const val EVENT_SUCCESSFUL = 1

        const val BIRTH_EVENT = 0
        const val READY_MATING_EVENT = 1
        const val MOVE_GROUP_EVENT = 2
        const val SLAUGHTER_EVENT = 3
    }

}
