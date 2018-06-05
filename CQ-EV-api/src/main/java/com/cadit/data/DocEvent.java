package com.cadit.data;


import java.util.GregorianCalendar;

/**
 * *****************************************************************************************
 * ********************************************API******************************************
 * *****************************************************************************************
 */
public class DocEvent {

    private GregorianCalendar eventTime;

    private Integer documentID;

    //obblig.
    public DocEvent() {
    }


    public DocEvent(Integer documentId,GregorianCalendar eventTime) {

        this.eventTime = eventTime;
        documentID = documentId;
    }

    public DocEvent(Integer documentId){
        GregorianCalendar instant = new GregorianCalendar();
        instant.setTimeInMillis(System.currentTimeMillis());
        documentID = documentId;
        this.eventTime = instant;
    }

    public Integer getDocumentID() {
        return documentID;
    }

    public GregorianCalendar getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        return "DocEvent{" +
                "eventTime=" + eventTime +
                ", documentID=" + documentID +
                '}';
    }
}
