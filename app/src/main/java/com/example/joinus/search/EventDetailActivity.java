package com.example.joinus.search;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    public static final String TAG = "EventDetail";
    FirebaseAuth mAuth;
    private String eventName, eventImg;
    private GeoPoint eventLocation;
    private Timestamp eventDate;
    private Integer eventAttendNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        String eventId = getIntent().getExtras().getString("eventId");
        mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();

        TextView event_attended = findViewById(R.id.event_detail_attended);
        TextView event_name = findViewById(R.id.event_detail_name);
        TextView event_date = findViewById(R.id.event_detail_date);
        TextView event_description = findViewById(R.id.event_detail_description);
        TextView event_location = findViewById(R.id.event_detail_location);

        ImageView event_img = findViewById(R.id.event_detail_img);

        Button event_add_btn = findViewById(R.id.event_add_btn);

        ProgressBar pb = findViewById(R.id.event_detail_pb);

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        pb.setVisibility(View.VISIBLE);

        //check if the user have subcollection "events"
        CollectionReference col = database.collection("users").document(currentUserId).collection("events");
        col.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().size() < 0){
                        event_attended.setVisibility(View.INVISIBLE);
                        event_add_btn.setVisibility(View.VISIBLE);
                    }else{
                        DocumentReference doc = col.document(eventId);
                        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        event_attended.setVisibility(View.VISIBLE);
                                        event_add_btn.setVisibility(View.INVISIBLE);
                                    } else {
                                        event_attended.setVisibility(View.INVISIBLE);
                                        event_add_btn.setVisibility(View.VISIBLE);
                                    }
                                } else {
                                    Log.d(TAG, "Failed with: ", task.getException());
                                }
                            }
                        });

                    }
                }else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

//        Log.d(TAG+"notification", String.valueOf(subcollection));
        DocumentReference docEvent = database.collection("events").document(eventId);
        docEvent.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Event event = document.toObject(Event.class);
                        eventName = event.getEventName();
                        eventLocation = event.getEventLocation();
                        eventDate = event.getEventDate();
                        eventImg = event.getEventImgURL();
                        eventAttendNum = event.getEventAttendNum();

                        event_name.setText("Event Name: " + eventName);
                        event_date.setText("Event Date: " + Utils.formatDate(eventDate.toDate()));
                        event_description.setText("Event Description: " + event.getEventDescription());
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
                        pb.setVisibility(View.INVISIBLE);
                    } else {
                        Log.d(TAG, "Document does not exist!");
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

        event_add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pb.setVisibility(View.VISIBLE);
                Event newEvent = new Event(eventId, eventName, eventLocation, eventDate, eventImg);
                database.collection("users").document(currentUserId).collection("events").document(eventId).set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"Add Event Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });

                DocumentReference docEvent = database.collection("events").document(eventId);
                database.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot snapshot = transaction.get(docEvent);
                        transaction.update(docEvent, "eventAttendNum", eventAttendNum + 1);
                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(EventDetailActivity.this,"Updated successfully!", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Transaction successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });
            }
        });
    }
}