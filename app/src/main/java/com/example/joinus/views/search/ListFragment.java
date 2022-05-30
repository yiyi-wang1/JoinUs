package com.example.joinus.views.search;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.joinus.R;
import com.example.joinus.viewmodel.EventViewModel;
import com.example.joinus.views.adapter.ListRecyclerAdapter;
import com.example.joinus.views.adapter.OnItemClickListener;
import com.example.joinus.model.Event;
import com.example.joinus.ShareViewModel.ShareViewModelResult;

import java.util.List;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";

    private ShareViewModelResult model;
    private Handler handler;
    private int distance;
    private EventViewModel eventViewModel;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    ListRecyclerAdapter listRecyclerAdapter;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        recyclerView = view.findViewById(R.id.search_list_result);
        model = new ViewModelProvider(requireActivity()).get(ShareViewModelResult.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String keyword = getActivity().getIntent().getExtras().getString("keyword");
        Double lat = getActivity().getIntent().getExtras().getDouble("lat");
        Double lon = getActivity().getIntent().getExtras().getDouble("lon");

        if(getArguments() != null){
            distance = getArguments().getInt("distance");
//            Log.d(TAG, String.valueOf(distance));
        }
        eventViewModel.getSearchResult(keyword,lat,lon,distance);
        eventViewModel.getSearchEventListMutableLiveData().observe(this, new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                if(events != null){
//                    Log.d(TAG, events.toString());
                    setView(events);
                }

            }
        });
    }

    //set the list view
    private void setView(List<Event> result){
        model.setList(result);
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        listRecyclerAdapter = new ListRecyclerAdapter(result, new OnItemClickListener() {
            @Override
            public void onItemClick(Event item) {
                Intent intent = new Intent(getActivity().getApplicationContext(), EventDetailActivity.class);
                intent.putExtra("eventId", item.getEventId());
                startActivity(intent);
            }
        }, getContext());
        recyclerView.setAdapter(listRecyclerAdapter);
        recyclerView.setLayoutManager(layoutManager);
    }
}