package com.example.joinus;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.joinus.model.Event;
import com.example.joinus.model.User;
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

public class Utils {

    public static final String TAG = "Utils";
    public static final String DEFAULTIMAGE = "DEFAULTIMAGE";
    public static final String USER_ERROR = "The user does not exists";
    public static final String[] TOPICS = new String[]{"Music", "Sports"};

    public final static User getUserData (String uid, Context context){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference reference = database.collection("users").document(uid);
        User currentUser = new User();
//        reference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot snapshot = task.getResult();
//                    //Log.d(TAG, "DocumentSnapshot data: " + task.getResult().toString());
//                    if (snapshot.exists()) {
//                        //Log.d(TAG, "DocumentSnapshot data: " + snapshot.getData());
//                        currentUser.setUid(uid);
//                        currentUser.setUsername(snapshot.getData().get("username").toString());
//                        currentUser.setEmail(snapshot.getData().get("email").toString());
//                        currentUser.setProfileImg(snapshot.getData().get("profileImg").toString());
//                        currentUser.setVerified((boolean) snapshot.getData().get("verified"));
//                        currentUser.setLocation((GeoPoint) snapshot.getData().get("location"));
//                        currentUser.setChatList((List<String>) snapshot.getData().get("chatList"));
//                        currentUser.setInterestedTopics((List<String>) snapshot.getData().get("interestedTopics"));
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });

        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    currentUser.setUid(uid);
                    currentUser.setUsername(snapshot.getData().get("username").toString());
                    currentUser.setEmail(snapshot.getData().get("email").toString());
                    currentUser.setProfileImg(snapshot.getData().get("profileImg").toString());
                    currentUser.setVerified((boolean) snapshot.getData().get("verified"));
                    currentUser.setLocation((GeoPoint) snapshot.getData().get("location"));
                    currentUser.setChatList((List<String>) snapshot.getData().get("chatList"));
                    currentUser.setInterestedTopics((List<String>) snapshot.getData().get("interestedTopics"));
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });

        //get the event list
        List<Event> eventList = new ArrayList<>();
        Query query = database.collection("users").document(uid).collection("events").orderBy("eventDate");
//        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Event event = document.toObject(Event.class);
//                        eventList.add(event);
//                        Log.d(TAG + "test", document.getId() + " => " + document.getData());
//                    }
//                }
//            }
//        });

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null) {
                    for (QueryDocumentSnapshot document : snapshot) {
                        Event event = document.toObject(Event.class);
                        eventList.add(event);
                        Log.d(TAG + "test", event.getEventName());
                    }
                    currentUser.setEventList(eventList);
                    Log.d(TAG + "eventlist", eventList.toString());
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
        return currentUser;
    }

    public static final String formatDate (Date date){
        android.text.format.DateFormat df = new android.text.format.DateFormat();
        return android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", date).toString();
    }
}
