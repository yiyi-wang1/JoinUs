package com.example.joinus.model.repository;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.Util.Utils;
import com.example.joinus.model.Event;
import com.example.joinus.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserResposity {
    private Application application;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private MutableLiveData<User> currentUserMutableLiveData;
    private MutableLiveData<Boolean> eventAttendedMutableLiveData;

    public static final String TAG = "USER_REPO";

    public UserResposity(Application application){
        this.application = application;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        currentUserMutableLiveData = new MutableLiveData<>();
        eventAttendedMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<User> getCurrentUserMutableLiveData() {
        return currentUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getEventAttendedMutableLiveData() {
        return eventAttendedMutableLiveData;
    }

    public void getUserData(){
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference reference = database.collection("users").document(uid);
        User currentUser = new User();
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                //get the user information from database
                if (snapshot != null && snapshot.exists()) {
                    currentUser.setUid(uid);
                    currentUser.setUsername(snapshot.getData().get("username").toString());
                    currentUser.setEmail(snapshot.getData().get("email").toString());
                    currentUser.setProfileImg(snapshot.getData().get("profileImg").toString());
                    currentUser.setVerified((boolean) snapshot.getData().get("verified"));
                    currentUser.setLocation((GeoPoint) snapshot.getData().get("location"));
                    currentUser.setInterestedTopics((List<String>) snapshot.getData().get("interestedTopics"));

                    //get the saved event list
                    List<Event> eventList = new ArrayList<>();
                    Query query = database.collection("users").document(uid).collection("events").orderBy("eventDate");

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
                                currentUserMutableLiveData.postValue(currentUser);
                                Log.d(TAG + "eventlist", eventList.toString());
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }

    public void updateUserData(String updateName, Uri updateImgUri, Set<String> updateInterested){
        String uid = mAuth.getCurrentUser().getUid();
        DocumentReference userRef = database.collection("users").document(uid);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userRef);
                transaction.update(userRef, "username", updateName);
                if(updateImgUri != null){
                    transaction.update(userRef, "profileImg", updateImgUri.toString());
                }
                if(updateInterested != null){
                    List<String> aList = new ArrayList<>(updateInterested);
                    transaction.update(userRef, "interestedTopics",aList);

                    //subscribe to topics
                    Utils.resetSubscription(aList,application);
                }
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(application,"Updated successfully!", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "Transaction successfully");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Transaction failure.", e);
            }
        });
    }

    public void addEventToUser(String eventId, String eventName, GeoPoint eventLocation, Timestamp eventDate, String eventImg){
        String uid = mAuth.getCurrentUser().getUid();
        Event newEvent = new Event(eventId, eventName, eventLocation, eventDate, eventImg);
        database.collection("users").document(uid).collection("events").document(eventId).set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(application,"Add Event Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void isAttendedEvent(String eventId){
        String uid = mAuth.getCurrentUser().getUid();
        CollectionReference col = database.collection("users").document(uid).collection("events");
        col.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().size() < 0){
                        eventAttendedMutableLiveData.postValue(false);
                    }else{
                        DocumentReference doc = col.document(eventId);
                        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        eventAttendedMutableLiveData.postValue(true);
                                    } else {
                                        eventAttendedMutableLiveData.postValue(false);
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
    }
}
