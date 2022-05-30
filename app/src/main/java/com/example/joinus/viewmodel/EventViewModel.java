package com.example.joinus.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.model.Event;
import com.example.joinus.model.repository.EventRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class EventViewModel extends AndroidViewModel {
    private EventRepository eventRepository;
    private MutableLiveData<Event> eventMutableLiveData;
    private MutableLiveData<List<Event>> hotEventListMutableLiveData;
    private MutableLiveData<List<Event>> Topic1EventListMutableLiveData;
    private MutableLiveData<List<Event>> Topic2EventListMutableLiveData;
    private MutableLiveData<List<Event>> searchEventListMutableLiveData;

    public EventViewModel(@NonNull Application application) {
        super(application);
        eventRepository = new EventRepository(application);
        eventMutableLiveData = eventRepository.getEventMutableLiveData();
        hotEventListMutableLiveData = eventRepository.getHotEventListMutableLiveData();
        Topic1EventListMutableLiveData = eventRepository.getTopic1EventListMutableLiveData();
        Topic2EventListMutableLiveData = eventRepository.getTopic2EventListMutableLiveData();
        searchEventListMutableLiveData = eventRepository.getSearchEventListMutableLiveData();
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

    public void createEvent(String eventId, String eventName, GeoPoint eventLocation, Timestamp eventDate, String eventImg, String eventTopic, String eventDescription){
        eventRepository.createEvent(eventId,eventName,eventLocation,eventDate,eventImg,eventTopic,eventDescription);
    }

    public void getSearchResult(String keyword,Double lat, Double lon, Integer distance){
        eventRepository.getSearchResult(keyword, lat, lon,distance);
    }

    public void getTopicList(String topic){
        eventRepository.getTopicList(topic);
    }

    public void getHotEventList(){
        eventRepository.getHotEventList();
    }

    public void updateEvent(String eventId, Integer eventAttendNum){
        eventRepository.updateEvent(eventId,eventAttendNum);
    }

    public void getEventDetail(String eventId){
        eventRepository.getEventDetail(eventId);
    }
}
