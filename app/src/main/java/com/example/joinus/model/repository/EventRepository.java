package com.example.joinus.model.repository;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.Util.Utils;
import com.example.joinus.model.Event;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventRepository {
    private Application application;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private MutableLiveData<Event> eventMutableLiveData;
    private MutableLiveData<List<Event>> hotEventListMutableLiveData;
    private MutableLiveData<List<Event>> Topic1EventListMutableLiveData;
    private MutableLiveData<List<Event>> Topic2EventListMutableLiveData;
    private MutableLiveData<List<Event>> searchEventListMutableLiveData;

    public EventRepository(Application application){
        this.application = application;
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();
        eventMutableLiveData = new MutableLiveData<>();
        hotEventListMutableLiveData = new MutableLiveData<>();
        Topic1EventListMutableLiveData = new MutableLiveData<>();
        Topic2EventListMutableLiveData = new MutableLiveData<>();
        searchEventListMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Event> getEventMutableLiveData() {
        return eventMutableLiveData;
    }

    public MutableLiveData<List<Event>> getHotEventListMutableLiveData() {
        return hotEventListMutableLiveData;
    }

    public MutableLiveData<List<Event>> getTopic1EventListMutableLiveData() {
        return Topic1EventListMutableLiveData;
    }

    public MutableLiveData<List<Event>> getTopic2EventListMutableLiveData() {
        return Topic2EventListMutableLiveData;
    }

    public MutableLiveData<List<Event>> getSearchEventListMutableLiveData() {
        return searchEventListMutableLiveData;
    }

    public void createEvent(String eventId, String eventName, GeoPoint eventLocation, Timestamp eventDate, String eventImg, String eventTopic,String eventDescription){
        //calculate the GeoHash
        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(eventLocation.getLatitude(), eventLocation.getLongitude()));
        String currentUserId = mAuth.getCurrentUser().getUid();
        Event newEvent = new Event(eventId, eventName,
                eventLocation, currentUserId ,eventDate,eventImg,1,eventTopic, eventDescription, hash);

        database.collection("events").document(eventId).set(newEvent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(application,"Create Event Successfully!", Toast.LENGTH_SHORT).show();
                Event newEvent1 = new Event(eventId, eventName, eventLocation, eventDate, eventImg);
                database.collection("users").document(currentUserId).collection("events").document(eventId).set(newEvent1).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(application,"Add Event Successfully!", Toast.LENGTH_SHORT).show();
                        eventMutableLiveData.postValue(newEvent);
                    }
                });
            }
        });
    }

    public void getSearchResult(String keyword,Double lat, Double lon, Integer distance){
        Query query = database.collection("events")
                .orderBy("eventName").startAt(keyword).endAt(keyword + "\uf8ff");

        List<Event> eventListByKeyword = new ArrayList<>();
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("Event Search", "Listen failed.", error);
                    return;
                }
                if (value != null) {
                    for (QueryDocumentSnapshot document : value) {
                        Event event = document.toObject(Event.class);
                        eventListByKeyword.add(event);
                    }
                }
            }
        });

        List<Event> eventListByLocation = new ArrayList<>();
        //this is the query to get nearby event list
        final GeoLocation center = new GeoLocation(lat, lon);
        final double radiusInM = distance * 1000;
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
                                    eventListByLocation.add(event);
                                }
                            }
                        }

                        if(eventListByLocation != null && eventListByKeyword != null) {
                            List<Event> result = new ArrayList<>();
                            for (Event e1 : eventListByKeyword) {
                                for (Event e2 : eventListByLocation) {
                                    if (e1.getEventId().equals(e2.getEventId()) && e1.getEventDate().toDate().compareTo(new Date()) > 0) {
                                        result.add(e1);
                                    }
                                }
                            }
                            Collections.sort(result, Comparator.comparing(e -> e.getEventDate().toDate()));
                            searchEventListMutableLiveData.postValue(result);
                        }

                    }
                });
    }

    public void getTopicList(String topic){
        List<Event> eventList = new ArrayList<>();
        Query queryTopic1 = database.collection("events")
                .whereEqualTo("eventTopic", topic)
                .whereGreaterThan("eventDate", Timestamp.now());

        queryTopic1.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("TopicList", "Listen failed.", error);
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
                    if(topic.equals(Utils.TOPICS[0])){
                        if(eventList.size() > 3){
                            List topicList = eventList.stream().limit(3).collect(Collectors.toList());
                            getTopic1EventListMutableLiveData().postValue(topicList);
                        }else{
                            getTopic1EventListMutableLiveData().postValue(eventList);
                        }
                    }else if(topic.equals(Utils.TOPICS[1])){
                        if(eventList.size() > 3){
                            List topicList = eventList.stream().limit(3).collect(Collectors.toList());
                            getTopic2EventListMutableLiveData().postValue(topicList);
                        }else{
                            getTopic2EventListMutableLiveData().postValue(eventList);
                        }
                    }
                } else {
                    Log.d("TopicList", "Current data: null");
                }
            }
        });
    }

    public void getHotEventList(){
        List<Event> eventList = new ArrayList<>();

        //get the event is not past
        Query query = database.collection("events")
                .whereGreaterThan("eventDate", Timestamp.now());
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Hot Event", "Listen failed.", error);
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
                        List topList = eventList.stream().limit(5).collect(Collectors.toList());
                        getHotEventListMutableLiveData().postValue(topList);
                    }else{
                        getHotEventListMutableLiveData().postValue(eventList);
                    }
                } else {
                    Log.d("Hot Event", "Current data: null");
                }
            }
        });
    }

    public void updateEvent(String eventId, Integer eventAttendNum){
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
//                pb.setVisibility(View.INVISIBLE);
                Toast.makeText(application,"Updated successfully!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("event", "Transaction failure.", e);
            }
        });
    }

    public void getEventDetail(String eventId){
        DocumentReference docEvent = database.collection("events").document(eventId);
        docEvent.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Event event = document.toObject(Event.class);
                        eventMutableLiveData.postValue(event);
                    } else {
                        Log.d("Event Detail", "Document does not exist!");
                    }
                } else {
                    Log.d("Event Detail", "Failed with: ", task.getException());
                }
            }
        });
    }
}


