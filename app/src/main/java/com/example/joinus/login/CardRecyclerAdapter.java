package com.example.joinus.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinus.Event;
import com.example.joinus.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerHolder> {
    private List<Event> eventList;

    public CardRecyclerAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public CardRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_card_item, parent, false);
        CardRecyclerHolder holder = new CardRecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CardRecyclerHolder holder, int position) {
        Event event = eventList.get(position);
        holder.name.setText(event.getEventName());
        holder.date.setText(event.getEventDate().toDate().toString());
        holder.num.setText(event.getEventAttendNum().toString());
        Picasso.get().load(event.getEventImgURL()).into(holder.img);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}




class CardRecyclerHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView date;
    public TextView num;
    public TextView txt;
    public ImageView img;

    public CardRecyclerHolder(View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.event_card_date_tv);
        name = itemView.findViewById(R.id.event_card_name_tv);
        num = itemView.findViewById(R.id.event_card_number_tv);
        img = itemView.findViewById(R.id.event_card_img);
        txt = itemView.findViewById(R.id.event_card_txt);
    }
}