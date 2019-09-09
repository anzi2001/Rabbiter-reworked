package com.example.kocja.rabbiter_online.adapters

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.databinding.InverseMethod
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.activities.AddEntryActivity
import kotlinx.android.synthetic.main.activity_add_entry.*
import java.lang.IllegalArgumentException


object BindingAdapters{

    @InverseMethod("fromString")
    fun toString(integer:Int): String{
        return integer.toString()
    }

    fun fromString(str:String):Int{
        return str.toIntOrNull() ?: 0
    }

    @BindingAdapter("spinnerData")
    @JvmStatic fun setSpinnerData(spinner: Spinner,value : String?){
        value?.let{
            spinner.setSelection((spinner.adapter as ArrayAdapter<String>).getPosition(value))
        }
    }
    @InverseBindingAdapter(attribute = "spinnerData")
    @JvmStatic fun getSpinnerData(spinner: Spinner) : String?{
        return spinner.selectedItem.toString()
    }

    @BindingAdapter("spinnerDataAttrChanged")
    @JvmStatic fun setListener(spinner: Spinner,inverseBindingListener: InverseBindingListener){
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val activity = view?.context as? AddEntryActivity ?: throw IllegalArgumentException("is not AddEntryActivity")
                activity.setValues()
                inverseBindingListener.onChange()
            }
        }

    }


}
