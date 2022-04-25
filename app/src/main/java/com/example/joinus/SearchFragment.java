package com.example.joinus;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.joinus.login.CardRecyclerAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";

    private ShareViewModel model;
    private User currentUser;
    private List<Event> eventList;

    private Handler handler;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CardRecyclerAdapter cardRecyclerAdapter;

    public SearchFragment() {
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ShareViewModel model = new ViewModelProvider(requireActivity()).get(ShareViewModel.class);
        currentUser = model.getCurrentUser().getValue();
        recyclerView = (RecyclerView) view.findViewById(R.id.search_hot_event_recommend_recyclerview);
        Log.d(TAG, currentUser.getLocation().toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler(Looper.getMainLooper());
        eventList = new ArrayList<>();


        FirebaseFirestore database = FirebaseFirestore.getInstance();
        Query query = database.collection("events").whereGreaterThan("eventDate", Timestamp.now());

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (value != null) {
                    for (QueryDocumentSnapshot document : value) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                        Log.d(TAG + "test", event.getEventName());
                    }
                    Collections.sort(eventList, new Comparator<Event>(){
                        @Override
                        public int compare(Event event, Event t1) {
                            return t1.getEventAttendNum() - event.getEventAttendNum();
                        }
                    });
                    setView();
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    public void setView(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), RecyclerView.HORIZONTAL, false);
                cardRecyclerAdapter = new CardRecyclerAdapter(eventList);
                recyclerView.setAdapter(cardRecyclerAdapter);
                recyclerView.setLayoutManager(layoutManager);
            }
        });
    }
}