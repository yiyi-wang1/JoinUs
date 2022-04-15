package com.example.joinus;

import android.media.Image;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;
    private String uid;
    private String username;
    private String profileImgUrl;
    private List<String> eventList;
    private List<Message> chatList;
    private Location location;
    private List<Topic> interestedTopics;
    private boolean verified;

    public User() {
    }

    public User(String email, String uid, String username) {
        this.email = email;
        this.uid = uid;
        this.username = username;
        profileImgUrl  = "default.png";
        eventList = new ArrayList<>();
        chatList = new ArrayList<>();
        location = new Location();
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

    public List<String> getEventList() {
        return eventList;
    }

    public void setEventList(List<String> eventList) {
        this.eventList = eventList;
    }

    public List<Message> getChatList() {
        return chatList;
    }

    public void setChatList(List<Message> chatList) {
        this.chatList = chatList;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<Topic> getInterestedTopics() {
        return interestedTopics;
    }

    public void setInterestedTopics(List<Topic> interestedTopics) {
        this.interestedTopics = interestedTopics;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
