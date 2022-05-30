package com.example.joinus.views.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.joinus.R;
import com.example.joinus.Util.Utils;
import com.example.joinus.model.Event;
import com.example.joinus.viewmodel.EventViewModel;
import com.example.joinus.viewmodel.UserViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {

    TextView event_attended;
    TextView event_name;
    TextView event_date;
    TextView event_description;
    TextView event_location;
    ImageView event_img;


    public static final String TAG = "EventDetail";
    private String eventName, eventImg, eventDescription;
    private GeoPoint eventLocation;
    private Timestamp eventDate;
    private Integer eventAttendNum;
    private UserViewModel userViewModel;
    private EventViewModel eventViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);

        String eventId = getIntent().getExtras().getString("eventId");
        event_attended = findViewById(R.id.event_detail_attended);
        event_name = findViewById(R.id.event_detail_name);
        event_date = findViewById(R.id.event_detail_date);
        event_description = findViewById(R.id.event_detail_description);
        event_location = findViewById(R.id.event_detail_location);

        event_img = findViewById(R.id.event_detail_img);

        Button event_add_btn = findViewById(R.id.event_add_btn);

        ProgressBar pb = findViewById(R.id.event_detail_pb);

        pb.setVisibility(View.VISIBLE);
        userViewModel.isAttendedEvent(eventId);
        userViewModel.getEventAttendedMutableLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean){
                    event_attended.setVisibility(View.VISIBLE);
                    event_add_btn.setVisibility(View.INVISIBLE);
                }else{
                    event_attended.setVisibility(View.INVISIBLE);
                    event_add_btn.setVisibility(View.VISIBLE);
                }
            }
        });

        eventViewModel.getEventDetail(eventId);
        eventViewModel.getEventMutableLiveData().observe(this, new Observer<Event>() {
            @Override
            public void onChanged(Event event) {
                if (event != null){
                    eventName = event.getEventName();
                    eventLocation = event.getEventLocation();
                    eventDate = event.getEventDate();
                    eventImg = event.getEventImgURL();
                    eventAttendNum = event.getEventAttendNum();
                    eventDescription = event.getEventDescription();
                    initView();
                    pb.setVisibility(View.INVISIBLE);
                }
            }
        });

        event_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userViewModel.addEventToUser(eventId,eventName,eventLocation,eventDate,eventImg);
                eventViewModel.updateEvent(eventId,eventAttendNum);
                userViewModel.isAttendedEvent(eventId);
            }
        });
    }

    private void initView(){
        event_name.setText("Event Name: " + eventName);
        event_date.setText("Event Date: " + Utils.formatDate(eventDate.toDate()));
        event_description.setText("Event Description: " + eventDescription);
        Picasso.get().load(eventImg).into(event_img);
        //get Location from geopoint
        double lat = eventLocation.getLatitude();
        double lon = eventLocation.getLongitude();
        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon,1);
            String location = addresses.get(0).getAddressLine(0);
            event_location.setText("Event Location: " + location);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}