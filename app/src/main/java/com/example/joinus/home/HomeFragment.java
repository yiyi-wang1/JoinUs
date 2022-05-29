package com.example.joinus.home;

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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.View.adapter.HomeRecyclerAdapter;
import com.example.joinus.model.Event;
import com.example.joinus.ShareViewModel.ShareViewModel;
import com.example.joinus.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    public final static String TAG = "HOME";

    private FirebaseAuth mAuth;
    private String uid;
    private User currentUser;
    private Handler handler;
    private ShareViewModel model;

    TextView username_tv;
    ProgressBar pb;
    RelativeLayout next_event_layout;
    Button creat_event_btn;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    HomeRecyclerAdapter homeRecyclerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        pb = (ProgressBar) view.findViewById(R.id.home_pb);
        username_tv = (TextView) view.findViewById(R.id.home_username_text);
        creat_event_btn = (Button) view.findViewById(R.id.create_event_btn);
        recyclerView = (RecyclerView) view.findViewById(R.id.attended_list_recyclerview);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler(Looper.getMainLooper());
        currentUser = null;
        pb.setVisibility(View.VISIBLE);

        new Thread(() -> {
            currentUser = Utils.getUserData(uid);

            while(currentUser.getUid() == null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, currentUser.toString());
                }
            }

            if(currentUser.getUsername() != null && currentUser.getEventList() != null){
                initView();
            }

        }).start();

        creat_event_btn.setOnClickListener(view -> {
            if(!currentUser.isVerified()){
                Toast.makeText(getContext(), "You are not verified yet!", Toast.LENGTH_SHORT).show();
            }else{
                Intent intent = new Intent(getActivity().getApplicationContext(), CreateEventActivity.class);
                startActivity(intent);
            }
        });
    }

    public void initView(){
        handler.post(() -> {
            //set the Textview
            username_tv.setText(currentUser.getUsername());
            pb.setVisibility(View.INVISIBLE);

            model = new ViewModelProvider(requireActivity()).get(ShareViewModel.class);
            model.setUser(currentUser);

            //set the view for next event
            int next_event_id;

            //if cannot get the event list or it is empty, show no next event
            if (currentUser.getEventList() == null || currentUser.getEventList().isEmpty()) {
                next_event_id = R.id.next_event_l1;
            } else {
                next_event_id = R.id.next_event_l2;
                Event nextEvent = find_next_event(currentUser.getEventList());
                //if all event are past, show no next event
                if(nextEvent == null){
                    next_event_id = R.id.next_event_l1;
                }else {
                    TextView next_event_time = getActivity().findViewById(R.id.next_event_time);
                    TextView next_event_name = getActivity().findViewById(R.id.next_event_name);
                    ImageView next_event_img = getActivity().findViewById(R.id.next_event_img);
                    next_event_time.setText(Utils.formatDate(nextEvent.getEventDate().toDate()));
                    next_event_name.setText(nextEvent.getEventName());
                    Picasso.get().load(nextEvent.getEventImgURL()).into(next_event_img);
                }
            }
            next_event_layout = getActivity().findViewById(next_event_id);
            next_event_layout.setVisibility(View.VISIBLE);

            //set the recycleview
            layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            homeRecyclerAdapter = new HomeRecyclerAdapter(currentUser.getEventList());
            recyclerView.setAdapter(homeRecyclerAdapter);
            recyclerView.setLayoutManager(layoutManager);

        });
    }

    /**
     * To find the next event
     * @param eventList
     * @return the next event
     */
    private Event find_next_event(List<Event> eventList){
      if (eventList.size() == 1){
          return eventList.get(0);
      }else{
          Event next = null;
          for(Event e : eventList){
              if(e.getEventDate().toDate().compareTo(new Date()) > 0){
                  Log.d(TAG, e.getEventName());
                  Log.d(TAG, e.getEventDate().toDate().toString());
                  Log.d(TAG, new Date().toString());
                  next = e;
                  return next;
              }
          }
          return next;
      }
    }
}