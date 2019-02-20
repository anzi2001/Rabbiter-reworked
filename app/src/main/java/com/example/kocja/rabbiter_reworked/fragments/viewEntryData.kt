package com.example.kocja.rabbiter_reworked.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.example.kocja.rabbiter_reworked.R
import com.example.kocja.rabbiter_reworked.activities.AddEvent
import com.example.kocja.rabbiter_reworked.databases.Entry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


/**
 * Created by kocja on 29/01/2018.
 */

class viewEntryData : Fragment() {
    private var entryName: TextView? = null
    private var entryGender: TextView? = null
    private var entryBirthDate: TextView? = null
    private var matedDateText: TextView? = null
    private var entryMatedWith: TextView? = null
    private var rabbitAge: TextView? = null
    private val fragmentFormat = SimpleDateFormat("dd/MM/yyyy", Locale.GERMANY)
    private var mainView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainView = inflater.inflate(R.layout.fragment_view_entry_data, container, false)
        entryName = mainView!!.findViewById(R.id.entryName)
        entryGender = mainView!!.findViewById(R.id.entryGender)
        entryBirthDate = mainView!!.findViewById(R.id.entryBirthDate)
        matedDateText = mainView!!.findViewById(R.id.entryMatedDateOrParents)
        entryMatedWith = mainView!!.findViewById(R.id.entryMatedWith)
        rabbitAge = mainView!!.findViewById(R.id.RabbitAge)
        mainView!!.findViewById<View>(R.id.AddMatingEvent).setOnClickListener {
            val startEventAdd = Intent(inflater.context, AddEvent::class.java)
            startActivity(startEventAdd)
        }
        return mainView
    }

    fun setData(entry: Entry?) {
        if(entry != null) {
            entryName!!.text = entry.entryName
            entryGender!!.text = entry.chooseGender
            if (entry.birthDate != null) {
                entryBirthDate!!.text = fragmentFormat.format(entry.birthDate)
            }
            entryMatedWith!!.text = entry.matedWithOrParents

            val matedWithText = mainView!!.findViewById<TextView>(R.id.matedWith)


            if (entry.matedDate != null && entry.chooseGender == "Group") {
                val parents = mainView!!.findViewById<TextView>(R.id.MatedDateOrParents)
                parents.text = getString(R.string.setParents)
                matedDateText!!.text = getString(R.string.Parents, entry.matedWithOrParents, entry.secondParent)
                //entryMatedWith.setVisibility(View.GONE);

            } else if (entry.matedDate != null) {
                matedDateText!!.text = fragmentFormat.format(entry.matedDate)
            }
            if (entry.birthDate != null) {
                var ageDate = TimeUnit.DAYS.convert(Date().time - entry.birthDate!!.time, TimeUnit.MILLISECONDS)
                val years = ageDate / 365
                ageDate %= 365
                val months = ageDate / 30
                ageDate %= 30
                rabbitAge!!.text = getString(R.string.setAge, years, months, ageDate)
            }
            if (entry.chooseGender == "Group") {
                entryMatedWith!!.text = entry.rabbitNumber.toString()
                matedWithText.text = getString(R.string.entryRabbitNum)
            }
        }
    }
}
