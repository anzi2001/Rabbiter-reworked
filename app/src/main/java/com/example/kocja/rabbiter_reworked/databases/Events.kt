package com.example.kocja.rabbiter_reworked.databases



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.activities.AddEntryActivity
import com.example.kocja.rabbiter_reworked.broadcastrecievers.NotifReciever
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by kocja on 28/01/2018.
 */
@Table(database = appDatabase::class)
class Events : BaseModel() {
    @PrimaryKey(autoincrement = true)
    var eventUUID: Int = 0
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
    var notificationState: Int = 0

    companion object {
        const val EVENT_FAILED = -1
        const val NOT_YET_ALERTED = 0
        const val EVENT_SUCCESSFUL = 1

        const val BIRTH_EVENT = 0
        const val READY_MATING_EVENT = 1
        const val MOVED_CAGE_EVENT = 2
        const val SLAUGHTERED_EVENT = 3

        private var defaultFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private lateinit var eventsManager : AlarmManager
        private val randGen = Random()
        //creates events for new entries
        fun create(rabbitEntry: Entry,context: Context) {
            eventsManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (rabbitEntry.chooseGender == context.getString(R.string.genderFemale)) {
                if (rabbitEntry.matedDate != null) {
                    val upcomingBirth = Date(rabbitEntry.matedDate!!.time + 1000L * 60 * 60 * 24 * 31)
                    newEvent(rabbitEntry, context.getString(R.string.femaleGaveBirth, defaultFormatter.format(upcomingBirth), rabbitEntry.entryName), upcomingBirth, AddEntryActivity.birthEvent,context)

                    val readyMateDate = Date(upcomingBirth.time + 1000L * 60 * 60 * 24 * 66)
                    newEvent(rabbitEntry, context.getString(R.string.femaleReadyForMating, defaultFormatter.format(readyMateDate), rabbitEntry.entryName), readyMateDate, AddEntryActivity.readyMatingEvent,context)
                }
            } else if (rabbitEntry.chooseGender == context.getString(R.string.genderGroup)) {
                if (rabbitEntry.birthDate != null) {
                    val moveDate = Date(rabbitEntry.birthDate!!.time + 1000L * 60 * 60 * 24 * 62)
                    newEvent(rabbitEntry, context.getString(R.string.groupMovedIntoCage, defaultFormatter.format(moveDate), rabbitEntry.entryName), moveDate, AddEntryActivity.moveGroupEvent,context)

                    val slaughterDate = Date(rabbitEntry.birthDate!!.time + 1000L * 60 * 60 * 24 * 124)
                    newEvent(rabbitEntry, context.getString(R.string.groupSlaughtered, defaultFormatter.format(slaughterDate), rabbitEntry.entryName), slaughterDate, AddEntryActivity.slaughterEvent,context)

                }
            }
        }

        private fun newEvent(rabbitEntry: Entry, eventString: String, dateOfEvent: Date, type: Int,context: Context) {
            val createEvent = Events()
            createEvent.name = rabbitEntry.entryName
            createEvent.eventString = eventString
            createEvent.dateOfEvent = dateOfEvent
            createEvent.typeOfEvent = type

            val alertEventService = Intent(context, NotifReciever::class.java)
            alertEventService.putExtra("eventUUID", createEvent.eventUUID)
            createEvent.id = randGen.nextInt()
            val slaughterEventAlarm = PendingIntent.getBroadcast(context, createEvent.id, alertEventService, PendingIntent.FLAG_CANCEL_CURRENT)
            eventsManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dateOfEvent.time, slaughterEventAlarm)

            createEvent.save()
        }
    }
}
