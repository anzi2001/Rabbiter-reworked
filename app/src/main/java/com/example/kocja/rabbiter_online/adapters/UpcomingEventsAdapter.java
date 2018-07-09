package com.example.kocja.rabbiter_online.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kocja.rabbiter_online.R;

import java.util.ArrayList;
import java.util.List;


public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.viewHolder> {
    private List<String> eventList;
    private onClickListen listener;
    private final boolean clickable;
    public UpcomingEventsAdapter(List<String> events,boolean clickable){
        eventList = new ArrayList<>(events.size());
        eventList = events;
        this.clickable = clickable;
    }

    class viewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final TextView eventString;
        viewHolder(View itemView) {
            super(itemView);
            eventString = itemView.findViewById(R.id.UpcomingText);
            eventString.setOnClickListener(this);
        }
         @Override
         public void onClick(View view) {
            if(clickable){
                listener.onItemClick(view,getAdapterPosition());
            }
         }
     }
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.upcoming_event_layout,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.eventString.setText(eventList.get(position));

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void setLongClickListener(onClickListen listen){
        listener = listen;
    }

    public interface onClickListen{
        void onItemClick(View view, int position);
    }

}
