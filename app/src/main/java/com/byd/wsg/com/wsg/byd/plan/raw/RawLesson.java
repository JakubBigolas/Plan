package com.byd.wsg.com.wsg.byd.plan.raw;

import android.support.annotation.NonNull;

/**
 * Created by Jakub on 2016-04-25.
 */
public class RawLesson {

    // TODO DO ZAMAPOWANIA POD RETROFITA

    public String title;

    public int start;

    public int end;

    public String with;

    public String room;

    public String type;

    public RawLesson(String title, int start, int end, String with, String room, String type) {
        this.title = title;
        this.start = start;
        this.end = end;
        this.with = with;
        this.room = room;
        this.type = type;
    }

    @NonNull
    public String getTitle() {
        return title == null ? "" : title;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @NonNull
    public String getWith() {
        return with == null ? "" : with;
    }

    @NonNull
    public String getRoom() {
        return room == null ? "" : room ;
    }

    @NonNull
    public String getType() {
        return type == null ? "" : type;
    }

    @Override
    public String toString() {
        return "RawLesson{" +
                "title='" + title + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", with='" + with + '\'' +
                ", room='" + room + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
