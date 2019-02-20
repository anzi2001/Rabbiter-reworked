package com.example.kocja.rabbiter_reworked.databases



import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import java.util.Date
import java.util.UUID

/**
 * Created by kocja on 28/01/2018.
 */
@Table(database = appDatabase::class)
class Events : BaseModel() {
    @PrimaryKey
    var eventUUID: UUID? = null
    @Column
    var name: String? = null
    @Column
    lateinit var secondParent: String
    @Column
    lateinit var eventString: String
    @Column
    var dateOfEvent: Date? = null
    @Column
    var rabbitsNum: Int = 0
    @Column
    var numDead: Int = 0
    @Column
    var id: Int = 0
    @Column
    var typeOfEvent: Int = 0
    @Column
    var timesNotified: Int = 0
    @Column
    var notificationState: Int = 0

    companion object {
        const val EVENT_FAILED = -1
        const val NOT_YET_ALERTED = 0
        const val EVENT_SUCCESSFUL = 1
    }

}
