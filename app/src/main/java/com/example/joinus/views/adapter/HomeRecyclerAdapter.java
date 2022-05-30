package com.example.joinus.views.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.model.Event;

import java.util.List;

public class HomeRecyclerAdapter extends RecyclerView.Adapter<HomeRecyclerHolder>{
    private List<Event> eventList;

    public HomeRecyclerAdapter(List<Event> eventList){
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public HomeRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        HomeRecyclerHolder holder = new HomeRecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerHolder holder, int position) {
        Event event = eventList.get(position);
        holder.name.setText(event.getEventName());
        holder.date.setText(Utils.formatDate(event.getEventDate().toDate()));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}


class HomeRecyclerHolder extends RecyclerView.ViewHolder{
    public TextView name;
    public TextView date;

    public HomeRecyclerHolder(View itemView){
        super(itemView);
        date = itemView.findViewById(R.id.event_time_tv);
        name = itemView.findViewById(R.id.event_name_tv);
    }
}
