package com.example.joinus;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class Event {

    private String eventId;
    private String eventName;
    private GeoPoint eventLocation;
    private String eventCreator;
    private Timestamp eventDate;
    private String eventImgURL;
    private Integer eventAttendNum;

    public Event() {
    }

    public Event(String eventId, String eventName, GeoPoint eventLocation, String eventCreator, Timestamp eventDate, String eventImgURL, Integer eventAttendNum) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventCreator = eventCreator;
        this.eventDate = eventDate;
        this.eventImgURL = eventImgURL;
        this.eventAttendNum = eventAttendNum;
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
}
