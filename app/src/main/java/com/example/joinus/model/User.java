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
    private String profileImgUrl;
    private List<Event> eventList;
    private List<String> chatList;
    private GeoPoint location;
    private List<String> interestedTopics;
    private boolean verified;

    public User() {
    }

    public User(String email, String uid, String username) {
        this.email = email;
        this.uid = uid;
        this.username = username;
        profileImgUrl  = Utils.DEFAULTIMAGE;
        eventList = new ArrayList<>();
        chatList = new ArrayList<>();
        location = null;
        interestedTopics = new ArrayList<>();
        verified = false;
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
        return profileImgUrl;
    }

    public void setProfileImg(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

    public List<String> getChatList() {
        return chatList;
    }

    public void setChatList(List<String> chatList) {
        this.chatList = chatList;
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
}
