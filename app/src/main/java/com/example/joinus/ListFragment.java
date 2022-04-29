package com.example.joinus;

import android.content.Intent;
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

import com.example.joinus.adapter.CardRecyclerAdapter;
import com.example.joinus.adapter.HomeRecyclerAdapter;
import com.example.joinus.adapter.ListRecyclerAdapter;
import com.example.joinus.adapter.OnItemClickListener;
import com.example.joinus.model.Event;
import com.example.joinus.model.ShareViewModelResult;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ListFragment extends Fragment {

    public static final String TAG = "ListFragment";

    private ShareViewModelResult model;
    private Handler handler;
    private List<Event> eventListByKeyword;
    private List<Event> eventListByLocation;

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
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        String keyword = getActivity().getIntent().getExtras().getString("keyword");
        Double lat = getActivity().getIntent().getExtras().getDouble("lat");
        Double lon = getActivity().getIntent().getExtras().getDouble("lon");
        handler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            Log.d(TAG+"keyword",keyword);

            getSearchResult(keyword);

            while(eventListByKeyword == null){
                try {
                    Thread.sleep(100);
                    Log.d(TAG+"Sleep", "wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            getLocationResult(lat, lon);

            while(eventListByLocation == null){
                try {
                    Thread.sleep(100);
                    Log.d(TAG+"Sleep", "wait");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(eventListByLocation != null && eventListByKeyword != null){
                List<Event> result = new ArrayList<>();
                for(Event e1 : eventListByKeyword){
                    for(Event e2 : eventListByLocation){
                        Log.d(TAG+"matching: ", e1.getEventId());
                        Log.d(TAG+"matching: ", e2.getEventId());
                        if(e1.getEventId().equals(e2.getEventId()) && e1.getEventDate().toDate().compareTo(new Date()) > 0){
                            result.add(e1);
                            Log.d(TAG + "result", result.toString());
                        }
                    }
                }
                setView(result);
            }
        }).start();
    }

    private void getSearchResult(String keyword){
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        //this is the query to get keyword event list
        Query query = database.collection("events")
                .orderBy("eventName").startAt(keyword).endAt(keyword + "\uf8ff");

        List<Event> eventList = new ArrayList<>();
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
                        Log.d(TAG+keyword,event.getEventId());
                    }
                    eventListByKeyword = eventList;
                }
            }
        });
    }

    private void getLocationResult(Double lat, Double lon){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        List<Event> eventList2 = new ArrayList<>();
        //this is the query to get nearby event list
        final GeoLocation center = new GeoLocation(lat, lon);
        final double radiusInM = 5 * 1000;
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = database.collection("events")
                    .orderBy("eventGeohash")
                    .startAt(b.startHash)
                    .endAt(b.endHash);
            tasks.add(q.get());
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                Event event = doc.toObject(Event.class);
                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                GeoLocation docLocation = new GeoLocation(event.getEventLocation().getLatitude(),event.getEventLocation().getLongitude());
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center);
                                if (distanceInM <= radiusInM) {
                                    eventList2.add(event);
                                    Log.d(TAG+"location",event.getEventId());
                                }
                            }
                        }
                        eventListByLocation = eventList2;
                    }
                });
    }

    private void setView(List<Event> result){
        handler.post(new Runnable() {
            @Override
            public void run() {
                //TODO: list recyclerview for result
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
        });
    }
}