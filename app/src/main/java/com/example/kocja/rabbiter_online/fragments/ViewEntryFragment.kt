package com.example.kocja.rabbiter_online.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.kocja.rabbiter_online.databinding.FragmentViewEntryDataBinding
import com.example.kocja.rabbiter_online.viewmodels.ViewEntryViewModel
import org.koin.android.viewmodel.ext.android.sharedViewModel


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
}
