package com.example.kocja.rabbiter_online.adapters

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.bumptech.glide.Glide
import com.example.kocja.rabbiter_online.managers.GsonManager
import com.example.kocja.rabbiter_online.managers.HttpManager
import com.example.kocja.rabbiter_online.R
import com.example.kocja.rabbiter_online.databases.Entry
import androidx.recyclerview.widget.RecyclerView
import de.hdodenhof.circleimageview.CircleImageView

class EntriesRecyclerAdapter(private val c: Activity, private val allEntries: List<Entry>) : RecyclerView.Adapter<EntriesRecyclerAdapter.viewHolder>() {
    private var listener: onItemClickListener? = null

    inner class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        val textName: TextView = itemView.findViewById(R.id.textName)
        val entryImage: CircleImageView = itemView.findViewById(R.id.entryImage)
        val mergedImage: CircleImageView = itemView.findViewById(R.id.mergedImage)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(view: View) {
            view.tag = allEntries[adapterPosition].entryID
            listener!!.onItemClick(view, adapterPosition)

        }

        override fun onLongClick(view: View): Boolean {
            view.tag = allEntries[adapterPosition].entryID
            listener!!.onLongItemClick(view, adapterPosition)
            return true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntriesRecyclerAdapter.viewHolder {
        val mainView = LayoutInflater.from(parent.context).inflate(R.layout.entries_adapter_entry, parent, false)

        return viewHolder(mainView)
    }

    override fun onBindViewHolder(holder: EntriesRecyclerAdapter.viewHolder, position: Int) {
        val entry = allEntries[position]
        if (entry.entryBitmap == null) {
            HttpManager.postRequest("searchForImage", GsonManager.getGson()!!.toJson(entry.entryPhLoc)) { _, bytes ->
                entry.entryBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
                loadDefaultIfNull(entry.entryBitmap, c, holder.entryImage)

            }
        } else {
            Glide.with(c)
                    .load(entry.entryBitmap)
                    .into(holder.entryImage)
        }

        if (entry.isMerged) {
            if (entry.mergedEntryBitmap == null) {
                HttpManager.postRequest("searchForImage", GsonManager.getGson()!!.toJson(entry.mergedEntryPhLoc)) { _, bytes1 ->
                    entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1, 0, bytes1!!.size)
                    loadDefaultIfNull(entry.mergedEntryBitmap, c, holder.mergedImage)

                }
            } else {
                Glide.with(c)
                        .load(entry.mergedEntryBitmap)
                        .into(holder.mergedImage)
            }
        }

        holder.entryImage.borderWidth = 6
        when {
            entry.chooseGender == "Female" -> holder.entryImage.borderColor = Color.parseColor("#EC407A")
            entry.chooseGender == "Male" -> holder.entryImage.borderColor = Color.BLUE
            else -> holder.entryImage.borderColor = Color.WHITE
        }

        if (entry.isMerged) {
            holder.mergedImage.visibility = View.VISIBLE
            holder.mergedImage.borderWidth = 4
            holder.mergedImage.borderColor = Color.WHITE

            holder.textName.text = c.getString(R.string.mergedStrings, entry.entryName, entry.mergedEntryName)
        } else {
            holder.textName.text = entry.entryName
        }

    }

    override fun getItemCount(): Int {
        return allEntries.size
    }

    fun setLongClickListener(itemClickListener: onItemClickListener) {
        listener = itemClickListener
    }

    interface onItemClickListener {
        fun onLongItemClick(view: View, position: Int)
        fun onItemClick(view: View, position: Int)
    }

    private fun loadDefaultIfNull(bitmap: Bitmap?, c: Activity, imageView: CircleImageView) {
        if (bitmap == null) {
            c.runOnUiThread {
                Glide.with(c)
                        .load(R.mipmap.dokoncana_ikona_zajec_round)
                        .into(imageView)
            }
        } else {
            c.runOnUiThread {
                Glide.with(c)
                        .load(bitmap)
                        .into(imageView)
            }
        }
    }
}
