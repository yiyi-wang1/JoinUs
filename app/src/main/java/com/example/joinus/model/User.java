package com.example.joinus.model;

import com.example.joinus.Utils;
import com.example.joinus.model.Event;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;
    private String uid;
    private String username;
    private String profileImg;
    private List<Event> eventList;
    private GeoPoint location;
    private List<String> interestedTopics;
    private boolean verified;
    private String fcmToken;

    public User() {
    }

    public User(String email, String uid, String username, String fcmToken) {
        this.email = email;
        this.uid = uid;
        this.username = username;
        profileImg  = Utils.DEFAULTIMAGE;
        eventList = null;
        location = null;
        interestedTopics = new ArrayList<>();
        verified = false;
        this.fcmToken = fcmToken;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public List<String> getInterestedTopics() {
        return interestedTopics;
    }

    public void setInterestedTopics(List<String> interestedTopics) {
        this.interestedTopics = interestedTopics;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
