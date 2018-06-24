package com.example.kocja.rabbiter_reworked.adapters;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kocja.rabbiter_reworked.GsonManager;
import com.example.kocja.rabbiter_reworked.HttpManager;
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
        Entry entry= allEntries.get(position);
        if(entry.entryBitmap == null){
            HttpManager.postRequest("searchForImage", GsonManager.getGson().toJson(entry.entryPhLoc), (response1, bytes) -> {
                entry.entryBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                c.runOnUiThread(() -> Glide.with(c)
                        .load(entry.entryBitmap)
                        .into(holder.entryImage));

            });
        }
        else{
            Glide.with(c)
                    .load(entry.entryBitmap)
                    .into(holder.entryImage);
        }

        if(entry.isMerged){
            if(entry.mergedEntryBitmap == null){
                HttpManager.postRequest("searchForImage", GsonManager.getGson().toJson(entry.mergedEntryPhLoc), (response2, bytes1) -> {
                    entry.mergedEntryBitmap = BitmapFactory.decodeByteArray(bytes1,0,bytes1.length);
                    c.runOnUiThread(() -> Glide.with(c)
                            .load(entry.mergedEntryBitmap)
                            .into(holder.mergedImage));
                });
            }
            else{
                Glide.with(c)
                        .load(entry.mergedEntryBitmap)
                        .into(holder.mergedImage);
            }
        }

        holder.entryImage.setBorderWidth(6);
        if (entry.chooseGender.equals("Female")) {
            holder.entryImage.setBorderColor(Color.parseColor("#EC407A"));
        }
        else if (entry.chooseGender.equals("Male")) {
            holder.entryImage.setBorderColor(Color.BLUE);
        }
        else {
            holder.entryImage.setBorderColor(Color.WHITE);
        }

        if(entry.isMerged){
            holder.mergedImage.setVisibility(View.VISIBLE);
            holder.mergedImage.setBorderWidth(4);
            holder.mergedImage.setBorderColor(Color.WHITE);

            holder.textName.setText(c.getString(R.string.mergedStrings,entry.entryName,entry.mergedEntryName));
        }
        else{
            holder.textName.setText(entry.entryName);
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
