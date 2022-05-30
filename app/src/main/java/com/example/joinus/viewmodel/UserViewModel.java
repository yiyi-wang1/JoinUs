package com.example.joinus.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.joinus.model.User;
import com.example.joinus.model.repository.UserResposity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Set;

public class UserViewModel extends AndroidViewModel {
    private UserResposity userResposity;
    private MutableLiveData<User> currentUserMutableLiveData;
    private MutableLiveData<Boolean> eventAttendedMutableLiveData;

    public UserViewModel(@NonNull Application application) {
        super(application);
        userResposity = new UserResposity(application);
        currentUserMutableLiveData = userResposity.getCurrentUserMutableLiveData();
        eventAttendedMutableLiveData = userResposity.getEventAttendedMutableLiveData();
    }

    public MutableLiveData<User> getCurrentUserMutableLiveData() {
        userResposity.getUserData();
        return currentUserMutableLiveData;
    }

    public MutableLiveData<Boolean> getEventAttendedMutableLiveData() {
        return eventAttendedMutableLiveData;
    }

    public void updateUserData(String updateName, Uri updateImgUri, Set<String> updateInterested){
        userResposity.updateUserData(updateName,updateImgUri,updateInterested);
    }

    public void addEventToUser(String eventId, String eventName, GeoPoint eventLocation, Timestamp eventDate, String eventImg){
        userResposity.addEventToUser(eventId,eventName,eventLocation,eventDate,eventImg);
    }

    public void isAttendedEvent(String eventId){
        userResposity.isAttendedEvent(eventId);
    }

}
