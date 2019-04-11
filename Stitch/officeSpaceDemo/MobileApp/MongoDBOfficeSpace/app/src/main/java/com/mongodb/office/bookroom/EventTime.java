package com.mongodb.office.bookroom;


import java.util.Date;

public class EventTime {
    private final Date dateTime;
    private final String timezone;


    public EventTime(Date dt) {
        dateTime=dt;
        timezone="Ireland/Dublin";

    }


}