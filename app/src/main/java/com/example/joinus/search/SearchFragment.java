package com.example.joinus.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.View.adapter.CardRecyclerAdapter;
import com.example.joinus.View.adapter.OnItemClickListener;
import com.example.joinus.model.Event;
import com.example.joinus.ShareViewModel.ShareViewModel;
import com.example.joinus.model.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SearchFragment extends Fragment {
    public static final String TAG = "SearchFragment";
    public static final String NO_LOCATION = "Your Location:  N/A          ";
    public static final String LOCATION = "Your Location: ";

    private ShareViewModel model;
    private User currentUser;
    private List<Event> topList;
    private List<Event> topicList1;
    private List<Event> topicList2;
    private String keyword;

    private Handler handler;
    FirebaseFirestore database;
    private LocationCallback callback;
    private FusedLocationProviderClient fusedLocationClient;

    //Search Area
    EditText search_bar;
    Button search_btn;

    //Location Area
    TextView search_location_tv;
    Button search_location_btn;
    ProgressBar progressBar;

    //Hot event recyclerView
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CardRecyclerAdapter cardRecyclerAdapter;

    //Topic View
    View topicView1;
    TextView topic_tv_1;
    RecyclerView topicRecyclerView1;
    RecyclerView.LayoutManager topicLayoutManager1;
    CardRecyclerAdapter topicRecyclerAdapter1;

    View topicView2;
    TextView topic_tv_2;
    RecyclerView topicRecyclerView2;
    RecyclerView.LayoutManager topicLayoutManager2;
    CardRecyclerAdapter topicRecyclerAdapter2;


    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(requireActivity()).get(ShareViewModel.class);
        currentUser = model.getCurrentUser().getValue();
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
        search_bar = view.findViewById(R.id.search_bar);
        search_btn = view.findViewById(R.id.search_btn);
        search_location_tv = view.findViewById(R.id.search_location);
        search_location_btn = view.findViewById(R.id.search_location_btn);
        progressBar = view.findViewById(R.id.search_location_pb);

        recyclerView = view.findViewById(R.id.search_hot_event_recommend_recyclerview);
        topicView1 = view.findViewById(R.id.search_top1);
        topicView2 = view.findViewById(R.id.search_top2);
        topic_tv_1 = topicView1.findViewById(R.id.topic_recommended_name);
        topic_tv_2 = topicView2.findViewById(R.id.topic_recommended_name);
        topicRecyclerView1 = topicView1.findViewById(R.id.topic_recommended_list_recyclerview);
        topicRecyclerView2 = topicView2.findViewById(R.id.topic_recommended_list_recyclerview);

    }

    public void setView(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                if(currentUser == null){
                    return;
                }
                if(currentUser.getLocation() == null){
                    search_location_tv.setText(NO_LOCATION);
                }else{
                    double lat = currentUser.getLocation().getLatitude();
                    double lon = currentUser.getLocation().getLongitude();
                    try {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat, lon,1);
                        search_location_tv.setText(LOCATION + addresses.get(0).getLocality());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                topic_tv_1.setText(Utils.TOPICS[0]);
                topic_tv_2.setText(Utils.TOPICS[1]);
                setRecyclerView(layoutManager, recyclerView, cardRecyclerAdapter,topList);
                setRecyclerView(topicLayoutManager1, topicRecyclerView1, topicRecyclerAdapter1,topicList1);
                setRecyclerView(topicLayoutManager2, topicRecyclerView2, topicRecyclerAdapter2,topicList2);
            }
        });
    }

    public void setRecyclerView(RecyclerView.LayoutManager layoutManager, RecyclerView recyclerView, RecyclerView.Adapter adapter, List<Event> list){
        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), RecyclerView.HORIZONTAL, false);
        adapter = new CardRecyclerAdapter(list, new OnItemClickListener() {
            @Override
            public void onItemClick(Event item) {
                Intent intent = new Intent(getActivity().getApplicationContext(), EventDetailActivity.class);
                intent.putExtra("eventId", item.getEventId());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler(Looper.getMainLooper());
        database = FirebaseFirestore.getInstance();
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            getHotEvent();
            getTopicEvents(Utils.TOPICS[0]);
            getTopicEvents(Utils.TOPICS[1]);

            while(topList == null && topicList1 == null && topicList2 == null){
                try {
                    Log.d(TAG+"Sleep","wait");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(topList != null && topicList1 != null && topicList2 != null){
                setView();
            }
        }).start();

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = search_bar.getText().toString().trim();
                Log.d(TAG,keyword);
                if(currentUser.getLocation() == null){
                    Toast.makeText(getActivity().getApplicationContext(),"Please get location", Toast.LENGTH_SHORT).show();
                }

                if(keyword != null && !TextUtils.isEmpty(keyword)){
                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchResultActivity.class);
                    intent.putExtra("keyword", keyword);
                    intent.putExtra("lat",currentUser.getLocation().getLatitude());
                    intent.putExtra("lon", currentUser.getLocation().getLongitude());
                    startActivity(intent);
                }else{
                    Toast.makeText(getActivity().getApplicationContext(),"Please enter search keyword", Toast.LENGTH_SHORT).show();
                }
            }
        });

        search_location_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!need_permission()){
                    getLocation();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        progressBar.setVisibility(View.VISIBLE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        LocationRequest request = LocationRequest.create();
        request.setInterval(10000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null){
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    try {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat, lon,1);
                        search_location_tv.setText(LOCATION + addresses.get(0).getLocality());

                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        DocumentReference userRef = database.collection("users").document(currentUser.getUid());
                        database.runTransaction(new Transaction.Function<Void>() {
                            @Override
                            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(userRef);
                                transaction.update(userRef, "location", new GeoPoint(lat,lon));
                                fusedLocationClient.removeLocationUpdates(callback);
                                progressBar.setVisibility(View.INVISIBLE);
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity().getApplicationContext(),"Updated successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure.", e);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper());
    }


    private boolean need_permission(){
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                getLocation();
            }else{
                Toast.makeText(getActivity(), "Permission Denied! Please enable the permission", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Permission Needed");
                builder.setMessage("Location Permission Needed");
                builder.setPositiveButton("Open Setting", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        getActivity().startActivity(intent);
                    }
                });
                builder.create().show();
            }
        }
    }

    public void getHotEvent(){
        List<Event> eventList = new ArrayList<>();

        //get the event is not past
        Query query = database.collection("events")
                        .whereGreaterThan("eventDate", Timestamp.now());
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
                    }
                    Collections.sort(eventList, new Comparator<Event>(){
                        @Override
                        public int compare(Event event, Event t1) {
                            return t1.getEventAttendNum() - event.getEventAttendNum();
                        }
                    });
                    if(eventList.size() > 5){
                        topList = eventList.stream().limit(5).collect(Collectors.toList());
                    }else{
                        topList = eventList;
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    public void getTopicEvents(String Topics){
        List<Event> eventList = new ArrayList<>();

        Query queryTopic1 = database.collection("events")
                            .whereEqualTo("eventTopic", Topics)
                            .whereGreaterThan("eventDate", Timestamp.now());

        queryTopic1.addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                    }
                    Collections.sort(eventList, new Comparator<Event>(){
                        @Override
                        public int compare(Event event, Event t1) {
                            return t1.getEventAttendNum() - event.getEventAttendNum();
                        }
                    });
                    if(Topics.equals(Utils.TOPICS[0])){
                        if(eventList.size() > 3){
                            topicList1 = eventList.stream().limit(3).collect(Collectors.toList());
                        }else{
                            topicList1 = eventList;
                        }
                    }else if(Topics.equals(Utils.TOPICS[1])){
                        if(eventList.size() > 3){
                            topicList2 = eventList.stream().limit(3).collect(Collectors.toList());
                        }else{
                            topicList2 = eventList;
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}