package com.example.joinus.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class Event {

    private String eventId;
    private String eventName;
    private GeoPoint eventLocation;
    private String eventCreator;
    private Timestamp eventDate;
    private String eventImgURL;
    private Integer eventAttendNum;
    private String eventTopic;
    private String eventDescription;
    private String eventGeohash;

    public Event() {
    }

    public Event(String eventId, String eventName, GeoPoint eventLocation, String eventCreator, Timestamp eventDate, String eventImgURL, Integer eventAttendNum, String eventTopic, String eventDescription, String eventGeohash) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventCreator = eventCreator;
        this.eventDate = eventDate;
        this.eventImgURL = eventImgURL;
        this.eventAttendNum = eventAttendNum;
        this.eventTopic = eventTopic;
        this.eventDescription = eventDescription;
        this.eventGeohash = eventGeohash;

    }

    public Event(String eventId, String eventName, GeoPoint eventLocation, Timestamp eventDate, String eventImgURL) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventDate = eventDate;
        this.eventImgURL = eventImgURL;
    }

    public Event(String eventId, String eventName, Timestamp eventDate, String eventImgURL) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventImgURL = eventImgURL;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public GeoPoint getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(GeoPoint eventLocation) {
        this.eventLocation = eventLocation;
    }

    public String getEventCreator() {
        return eventCreator;
    }

    public void setEventCreator(String eventCreator) {
        this.eventCreator = eventCreator;
    }

    public Timestamp getEventDate() {
        return eventDate;
    }

    public void setEventDate(Timestamp eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventImgURL() {
        return eventImgURL;
    }

    public void setEventImgURL(String eventImgURL) {
        this.eventImgURL = eventImgURL;
    }

    public Integer getEventAttendNum() {
        return eventAttendNum;
    }

    public void setEventAttendNum(Integer eventAttendNum) {
        this.eventAttendNum = eventAttendNum;
    }

    public String getEventTopic() {
        return eventTopic;
    }

    public void setEventTopic(String eventTopic) {
        this.eventTopic = eventTopic;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventGeohash() {
        return eventGeohash;
    }

    public void setEventGeohash(String eventGeohash) {
        this.eventGeohash = eventGeohash;
    }
}
