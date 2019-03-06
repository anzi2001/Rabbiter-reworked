package com.example.kocja.rabbiter_reworked.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.fragment.app.Fragment

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.activities.ViewAllMatings
import com.example.kocja.rabbiter_reworked.databases.*
import com.raizlabs.android.dbflow.config.FlowManager
import kotlinx.android.synthetic.main.fragment_view_entry_data.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by kocja on 29/01/2018.
 */

class viewEntryData : Fragment(), DatePickerDialog.OnDateSetListener {
    private val fragmentFormat = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
    private lateinit var datePicker : DatePickerDialog
    private lateinit var viewAllIntent : Intent
    private lateinit var entry : Entry

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mainView = inflater.inflate(R.layout.fragment_view_entry_data, container, false)
        viewAllIntent = Intent(inflater.context, ViewAllMatings::class.java)
        val calendar = Calendar.getInstance()
        datePicker = DatePickerDialog(inflater.context,this@viewEntryData,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        return mainView
    }

    fun setData(entry: Entry?) {
        if(entry == null){
            return
        }
        this.entry = entry
        AddMatingEvent.setOnClickListener {
            datePicker.show()
        }
        viewAllMatings.setOnClickListener {
            viewAllIntent.putExtra("entryID",entry.entryID)
            startActivity(viewAllIntent)
        }

        entryName!!.text = entry.entryName
        entryGender!!.text = entry.chooseGender

        if (entry.birthDate != null) {
            entryBirthDate!!.text = fragmentFormat.format(entry.birthDate)
        }
        entryMatedWith!!.text = entry.matedWithOrParents
        if (entry.matedDate != null && entry.chooseGender == "Group") {
            MatedDateOrParents.text = getString(R.string.setParents)
            entryMatedDateOrParents!!.text = getString(R.string.Parents, entry.matedWithOrParents, entry.secondParent)
        } else if (entry.matedDate != null) {
            entryMatedDateOrParents!!.text = fragmentFormat.format(entry.matedDate)
        }
        if (entry.birthDate != null) {
            var ageDate = TimeUnit.DAYS.convert(Date().time - entry.birthDate!!.time, TimeUnit.MILLISECONDS)
            val years = ageDate / 365
            ageDate %= 365
            val months = ageDate / 30
            ageDate %= 30
            RabbitAge!!.text = getString(R.string.setAge, years, months, ageDate)
        }
        if (entry.chooseGender == "Group") {
            entryMatedWith!!.text = entry.rabbitNumber.toString()
            entryMatedWith.text = getString(R.string.entryRabbitNum)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val matingDate = GregorianCalendar(year,month,dayOfMonth).time
        val mating = Matings()
        mating.matingUUID = UUID.randomUUID()
        mating.entryID = entry.entryID
        mating.matingDate = matingDate
        FlowManager.getDatabase(appDatabase::class.java).beginTransactionAsync {
            mating.save()
        }.build().execute()

        Events.create(entry,context!!)

    }
}
