package com.example.kocja.rabbiter_online.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databinding.ContentRabbitLinearFemaleBinding
import com.example.kocja.rabbiter_online.databinding.ContentRabbitLinearMaleGroupBinding
import com.example.kocja.rabbiter_online.models.Entry


class EntriesRecyclerAdapter(private val c: Context, private val allEntries: List<Entry>) : RecyclerView.Adapter<EntriesRecyclerAdapter.ViewHolder>() {
    private var clickListener : View.OnClickListener? = null
    private var longClickListener : View.OnLongClickListener? = null

    inner class ViewHolder(val item: ViewBinding) : RecyclerView.ViewHolder(item.root)

    override fun getItemViewType(position: Int): Int {
        return when(allEntries[position].chooseGender){
            "Female"->  R.layout.content_rabbit_linear_female
            else-> R.layout.content_rabbit_linear_male_group
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        lateinit var binder : ViewBinding
        val mainView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        mainView.setOnClickListener(clickListener)
        mainView.setOnLongClickListener(longClickListener)
        binder = if(viewType == R.layout.content_rabbit_linear_female){
            ContentRabbitLinearFemaleBinding.bind(mainView)
        } else{
            ContentRabbitLinearMaleGroupBinding.bind(mainView)
        }

        return ViewHolder(binder)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.item.root){
            tag = position

            rabbitView.load(allEntries[position].entryPhotoURL){
                crossfade(500)
            }

            Gender.text = allEntries[position].chooseGender

            allEntries[position].birthDate?.let{
                birthDate.text = it
            }

            if(matingDate != null){
                matingDate?.text = allEntries[position].matedDate ?: ""
            }

            rabbitView.clipToOutline = true
            Gender.setTextColor(when (allEntries[position].chooseGender) {
                "Female" -> Color.parseColor("#EC407A")
                "Male" -> Color.BLUE
                else -> Color.DKGRAY
            })

            Title.text = if (allEntries[position].isMerged) {
                mergedView.visibility = View.VISIBLE
                mergedView.load(allEntries[position].mergedEntryPhotoURL)
                c.getString(R.string.mergedStrings, allEntries[position].entryName, allEntries[position].mergedEntryName)
            } else {
                mergedView.visibility = View.GONE
                allEntries[position].entryName
            }
        }

    }


    override fun getItemCount() = allEntries.size

    fun setClickListeners(itemClickListener: View.OnClickListener, longItemClickListener : View.OnLongClickListener) {
        clickListener = itemClickListener
        longClickListener = longItemClickListener
    }
}
