package com.example.kocja.rabbiter_online.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.models.Entry
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.entries_adapter_entry.view.*

class EntriesRecyclerAdapter(private val c: Context, private val allEntries: List<Entry>) : RecyclerView.Adapter<EntriesRecyclerAdapter.ViewHolder>() {
    private lateinit var listener: OnItemClickListener

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            view.tag = allEntries[adapterPosition].entryID
            listener.onItemClick(view, adapterPosition)

        }

        override fun onLongClick(view: View): Boolean {
            view.tag = allEntries[adapterPosition].entryID
            listener.onLongItemClick(view, adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mainView = LayoutInflater.from(parent.context).inflate(R.layout.entries_adapter_entry, parent, false)

        return ViewHolder(mainView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = allEntries[position]
        if (entry.entryBitmap == null) {
            HttpManager.postRequest("searchForImage", GsonManager.gson.toJson(entry.entryPhLoc)) { _, bytes ->
                entry.entryBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                loadDefaultIfNull(entry.entryBitmap, c, holder.itemView.entryImage)

            }
        } else {
            Glide.with(c)
                    .load(entry.entryBitmap)
                    .into(holder.itemView.entryImage)
        }

        if (entry.isMerged) {
            if (entry.mergedEntryBitmap == null) {
                HttpManager.postRequest("searchForImage", GsonManager.gson.toJson(entry.mergedEntryPhLoc)) { _, bytes1 ->
                    entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1, 0, bytes1!!.size)
                    loadDefaultIfNull(entry.mergedEntryBitmap, c, holder.itemView.mergedImage)

                }
            } else {
                Glide.with(c)
                        .load(entry.mergedEntryBitmap)
                        .into(holder.itemView.mergedImage)
            }
        }

        holder.itemView.entryImage.borderWidth = 6
        when {
            entry.chooseGender == "Female" -> holder.itemView.entryImage.borderColor = Color.parseColor("#EC407A")
            entry.chooseGender == "Male" -> holder.itemView.entryImage.borderColor = Color.BLUE
            else -> holder.itemView.entryImage.borderColor = Color.WHITE
        }

        holder.itemView.textName.text = if (entry.isMerged) {
            with(holder.itemView.mergedImage){
                visibility = View.VISIBLE
                borderWidth = 4
                borderColor = Color.WHITE
            }

            c.getString(R.string.mergedStrings, entry.entryName, entry.mergedEntryName)
        } else {
            entry.entryName
        }

    }

    override fun getItemCount(): Int {
        return allEntries.size
    }

    fun setLongClickListener(itemClickListener: OnItemClickListener) {
        listener = itemClickListener
    }

    interface OnItemClickListener {
        fun onLongItemClick(view: View, position: Int)
        fun onItemClick(view: View, position: Int)
    }

    private fun loadDefaultIfNull(bitmap: Bitmap?, c: Context, imageView: CircleImageView) {
        Handler(c.mainLooper).post{
            if (bitmap == null) {
                Glide.with(c)
                        .load(R.mipmap.dokoncana_ikona_zajec_round)
                        .into(imageView)
            } else {
                Glide.with(c)
                        .load(bitmap)
                        .into(imageView)
            }
        }

    }
}
