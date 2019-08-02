package com.example.kocja.rabbiter_online.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.FragmentViewEntryDataBinding
import com.example.kocja.rabbiter_online.models.Entry
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryFragmentViewModel
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import kotlinx.android.synthetic.main.fragment_view_entry_data.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel


/**
 * Created by kocja on 29/01/2018.
 */

class ViewEntryFragment : Fragment() {
    private val viewEntryViewModel : ViewEntryViewModel by sharedViewModel()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentViewEntryDataBinding.inflate(inflater,container,false)
        binding.lifecycleOwner = this
        binding.viewEntryViewModel = viewEntryViewModel
        return binding.root
    }

    fun setData(entry: Entry) {
        /*entryName.text = entry.entryName
        entryGender.text = entry.chooseGender
        if (entry.birthDate != null) {
            entryBirthDate.text = entry.birthDate
        }
        entryMatedWith.text = entry.matedWithOrParents


        entryMatedDateOrParents.text = if (entry.matedDate != null && entry.chooseGender == "Group") {
            MatedDateOrParents.text = getString(R.string.setParents)
            getString(R.string.Parents, entry.matedWithOrParents, entry.secondParent)
            //entryMatedWith.setVisibility(View.GONE);

        } else{
            entry.matedDate
        }
        /*if(!entry.birthDate.equals("")) {
            long ageDate = 0;

            try {
                ageDate = TimeUnit.DAYS.convert((new Date().getTime() - fragmentFormat.parse(entry.birthDate).getTime()),TimeUnit.MILLISECONDS);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            long years = ageDate / 365;
            ageDate = ageDate % 365;
            long months = ageDate / 30;
            ageDate = ageDate %30;
            rabbitAge.setText(getString(R.string.setAge, years, months, ageDate));
        }*/
        if (entry.chooseGender == "Group") {
            /*rabbitNumText.setVisibility(View.VISIBLE);
            rabbitNum.setVisibility(View.VISIBLE);
            rabbitNum.setText(Integer.toString(entry.rabbitNumber));*/
            entryMatedWith.text = entry.rabbitNumber.toString()
            matedWith.text = getString(R.string.entryRabbitNum)
        }*/
    }
}
