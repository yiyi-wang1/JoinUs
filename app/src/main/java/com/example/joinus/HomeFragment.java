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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    public final static String TAG = "home";

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

        new Thread(() -> {
            pb.setVisibility(View.VISIBLE);
            currentUser = Utils.getUserData(uid,getContext());

            while(currentUser.getUid() == null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if(currentUser.getUsername() != null && currentUser.getEventList() != null){
                initView();
            }
        }).start();

        creat_event_btn.setOnClickListener(view -> {
            if(!currentUser.isVerified()){
                Toast.makeText(getContext(), "You are not verified yet!", Toast.LENGTH_SHORT);
            }else{
                //TODO: start the new event create page
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

            //Log.e(TAG, "username test: " + currentUser.getUsername());
            if (currentUser.getEventList() == null || currentUser.getEventList().isEmpty()) {
                next_event_id = R.id.next_event_l1;
            } else {
                next_event_id = R.id.next_event_l2;
            }
            next_event_layout = getActivity().findViewById(next_event_id);
            next_event_layout.setVisibility(View.VISIBLE);

            Event nextEvent = find_next_event(currentUser.getEventList());

            TextView next_event_time = getActivity().findViewById(R.id.next_event_time);
            TextView next_event_name = getActivity().findViewById(R.id.next_event_name);
            ImageView next_event_img = getActivity().findViewById(R.id.next_event_img);

            next_event_time.setText(nextEvent.getEventDate().toDate().toString());
            next_event_name.setText(nextEvent.getEventName());


            //set the recycleview
            layoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            homeRecyclerAdapter = new HomeRecyclerAdapter(currentUser.getEventList());
            recyclerView.setAdapter(homeRecyclerAdapter);
            recyclerView.setLayoutManager(layoutManager);
        });
    }

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