package com.example.joinus.adapter;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.joinus.R;
import com.example.joinus.Utils;
import com.example.joinus.model.Event;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ListRecyclerAdapter extends RecyclerView.Adapter<ListRecyclerHolder>{
    private List<Event> eventList;
    private OnItemClickListener listener;
    private Context context;

    public ListRecyclerAdapter(List<Event> eventList, OnItemClickListener listener, Context context) {
        this.eventList = eventList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ListRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_result_event_card_item, parent, false);
        ListRecyclerHolder holder = new ListRecyclerHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListRecyclerHolder holder, int position) {
        holder.bind(context, eventList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}




class ListRecyclerHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView date;
    public TextView num;
    public TextView txt;
    public TextView location_tv;

    public ListRecyclerHolder(View itemView) {
        super(itemView);
        date = itemView.findViewById(R.id.event_card_date_tv);
        name = itemView.findViewById(R.id.event_card_name_tv);
        num = itemView.findViewById(R.id.event_card_number_tv);
        txt = itemView.findViewById(R.id.event_card_txt);
        location_tv = itemView.findViewById(R.id.event_card_location);
    }

    public void bind(Context context, final Event event, final OnItemClickListener listener) {

        //get Location from geopoint
        double lat = event.getEventLocation().getLatitude();
        double lon = event.getEventLocation().getLongitude();
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon,1);
            String location = addresses.get(0).getAddressLine(0);
            Log.d("list", location);
            location_tv.setText(location);
        } catch (IOException e) {
            e.printStackTrace();
        }

        name.setText(event.getEventName());
        date.setText(Utils.formatDate(event.getEventDate().toDate()));
        num.setText(event.getEventAttendNum().toString());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                listener.onItemClick(event);
            }
        });
    }
}
