package com.mongodb.office.bookroom;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class Event {

    private ObjectId id;
    private String owner_id;
    private String summary;
    private String location;
    private String description;
    private ArrayList attendees;
    private EventTime start;
    private EventTime end;

    public Event() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(final String oi) {
        this.owner_id = oi;
    }

    public String getSummary() {
        return summary;
    }
    public void setSummary(final String s) {
        this.summary = s;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String d) {
        this.description = d;
    }



    public String getLocation() {
        return location;
    }

    public void setLocation(final String l) {
        this.location = l;
    }

    public EventTime getStart() {
        return start;
    }

    public void setStart(final EventTime s) {
        this.start = s;
    }

    public EventTime getEnd() {
        return end;
    }

    public void setEnd(final EventTime e) {
        this.end = e;
    }


    public ArrayList getAttendees() {
        return attendees;
    }

    public void setAttendees(final ArrayList at) {
        this.attendees = at;
    }
}
