package com.mongodb.office.bookroom;


import java.util.Date;

public class EventTime {
    private  Date dateTime;
    private  String timeZone;


    public EventTime() {

    }

    public Date getDateTime(){
        return dateTime;
    }
    public void setDateTime(final Date dt){
        dateTime=dt;
    }


    public String getTimeZone(){
        return timeZone;
    }
    public void setTimeZone(final String tz){
        timeZone=tz;
    }
}