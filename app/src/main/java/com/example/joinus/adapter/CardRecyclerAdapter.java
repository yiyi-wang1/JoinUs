package com.example.joinus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinus.R;
import com.example.joinus.Utils;
import com.example.joinus.model.Event;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CardRecyclerAdapter extends RecyclerView.Adapter<CardRecyclerHolder> {
    private List<Event> eventList;
    private OnItemClickListener listener;

    public CardRecyclerAdapter(List<Event> eventList, OnItemClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
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
        holder.bind(eventList.get(position), listener);
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

    public void bind(final Event event, final OnItemClickListener listener) {
        name.setText(event.getEventName());
        date.setText(Utils.formatDate(event.getEventDate().toDate()));
        num.setText(event.getEventAttendNum().toString());
        Picasso.get().load(event.getEventImgURL()).into(img);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick(event);
            }
        });
    }

}