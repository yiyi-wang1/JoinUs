package com.example.joinus.views.search;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
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
import com.example.joinus.viewmodel.EventViewModel;
import com.example.joinus.viewmodel.LocationViewModel;
import com.example.joinus.views.adapter.CardRecyclerAdapter;
import com.example.joinus.views.adapter.OnItemClickListener;
import com.example.joinus.model.Event;
import com.example.joinus.ShareViewModel.ShareViewModel;
import com.example.joinus.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
    private FirebaseFirestore database;
    private LocationViewModel locationViewModel;
    private EventViewModel eventViewModel;

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
        locationViewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
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
        setView();
            eventViewModel.getHotEventList();
            eventViewModel.getHotEventListMutableLiveData().observe(this, new Observer<List<Event>>() {
                @Override
                public void onChanged(List<Event> events) {
                    if(events != null){
                        topList = events;
                        setRecyclerView(layoutManager, recyclerView, cardRecyclerAdapter,topList);
                    }
                }
            });
            eventViewModel.getTopicList(Utils.TOPICS[0]);
            eventViewModel.getTopic1EventListMutableLiveData().observe(this, new Observer<List<Event>>() {
                @Override
                public void onChanged(List<Event> events) {
                    if(events != null){
                        topicList1 = events;
                        setRecyclerView(topicLayoutManager1, topicRecyclerView1, topicRecyclerAdapter1,topicList1);
                    }
                }
            });
            eventViewModel.getTopicList(Utils.TOPICS[1]);
            eventViewModel.getTopic2EventListMutableLiveData().observe(this, new Observer<List<Event>>() {
                @Override
                public void onChanged(List<Event> events) {
                    if(events != null){
                        topicList2 = events;
                        setRecyclerView(topicLayoutManager2, topicRecyclerView2, topicRecyclerAdapter2,topicList2);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }
            });
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                keyword = search_bar.getText().toString().trim();
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
        locationViewModel.getLocation();
        locationViewModel.getLocationMutableLiveData().observe(this, new Observer<Location>() {
            @Override
            public void onChanged(Location location) {
                if(location != null){
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        addresses = geocoder.getFromLocation(lat, lon,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    search_location_tv.setText(LOCATION + addresses.get(0).getLocality());
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
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
}