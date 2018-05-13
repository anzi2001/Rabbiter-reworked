package com.example.kocja.rabbiter_reworked.adapters;

import android.app.Activity;
import android.graphics.Color;
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

public class EntriesRecyclerAdapter extends RecyclerView.Adapter<EntriesRecyclerAdapter.viewHolder>{
    private final Activity c;
    private final List<Entry> allEntries;
    private onItemClickListener listener;

    public EntriesRecyclerAdapter(Activity mainC, List<Entry> entries){
        allEntries = entries;
        c = mainC;
    }

    class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        TextView textName;
        CircleImageView entryImage;
        CircleImageView mergedImage;
        viewHolder(View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            entryImage = itemView.findViewById(R.id.entryImage);

            //if(allEntries.get(getAdapterPosition()).isMerged){
                mergedImage = itemView.findViewById(R.id.mergedImage);
            //}
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
        @Override
        public void onClick(View view){
            view.setTag(allEntries.get(getAdapterPosition()).entryID);
            listener.onItemClick(view,getAdapterPosition());

        }

        @Override
        public boolean onLongClick(View view) {
            view.setTag(allEntries.get(getAdapterPosition()).entryID);
            listener.onLongItemClick(view,getAdapterPosition());
            return true;
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

        Glide.with(c)
                .load(allEntries.get(position).entryPhLoc)
                .into(holder.entryImage);

        holder.entryImage.setBorderWidth(6);
        if (allEntries.get(position).chooseGender.equals("Female")) {
            holder.entryImage.setBorderColor(Color.parseColor("#EC407A"));
        }
        else if (allEntries.get(position).chooseGender.equals("Male")) {
            holder.entryImage.setBorderColor(Color.BLUE);
        }
        else {
            holder.entryImage.setBorderColor(Color.WHITE);
        }

        if(allEntries.get(position).isMerged){
            holder.mergedImage.setVisibility(View.VISIBLE);
            holder.mergedImage.setBorderWidth(4);
            holder.mergedImage.setBorderColor(Color.WHITE);
            Glide.with(c)
                    .load(allEntries.get(position).mergedEntryPhLoc)
                    .into(holder.mergedImage);
            holder.textName.setText(c.getString(R.string.mergedStrings,allEntries.get(position).entryName,allEntries.get(position).mergedEntryName));
        }
        else{
            holder.textName.setText(allEntries.get(position).entryName);
        }

    }

    @Override
    public int getItemCount() {
        return allEntries.size();
    }

    public void setLongClickListener(onItemClickListener itemClickListener){
        listener = itemClickListener;
    }
    public interface onItemClickListener{
        void onLongItemClick(View view, int position);
        void onItemClick(View view,int position);
    }
}
