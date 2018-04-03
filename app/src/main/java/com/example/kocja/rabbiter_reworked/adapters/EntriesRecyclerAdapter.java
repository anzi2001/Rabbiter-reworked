package com.example.kocja.rabbiter_reworked.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.R;
import com.example.kocja.rabbiter_reworked.databases.Entry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EntriesRecyclerAdapter extends RecyclerView.Adapter<EntriesRecyclerAdapter.viewHolder> {
    private final Context c;
    private List<Entry> allEntries;
    public EntriesRecyclerAdapter(Context mainC, List<Entry> entries){
        allEntries.addAll(entries);
        c = mainC;
    }

    class viewHolder extends RecyclerView.ViewHolder{
        TextView textName;
        CircleImageView entryImage;
        CircleImageView mergedImage;
        viewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            entryImage = itemView.findViewById(R.id.entryImage);
            if(allEntries.get(getAdapterPosition()).isMerged){
                mergedImage = itemView.findViewById(R.id.mergedImage);
            }
        }
    }
    @NonNull
    @Override
    public EntriesRecyclerAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mainView = LayoutInflater.from(parent.getContext()).inflate(R.layout.entries_adapter_entry,parent,false);
        return new viewHolder(mainView);
    }

    @Override
    public void onBindViewHolder(@NonNull EntriesRecyclerAdapter.viewHolder holder, int position) {
        holder.textName.setText(allEntries.get(position).entryName);
        Glide.with(c).load(allEntries.get(position).entryPhLoc).into(holder.entryImage);
        if(allEntries.get(position).isMerged){
            Glide.with(c).load(allEntries.get(position).mergedEntryPhLoc).into(holder.mergedImage);
        }

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
