package com.byd.wsg.com.wsg.byd.plan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Jakub on 2016-04-23.
 */
public class Lesson extends Interval {

    private String room;
    private Subject subject;

    @Override
    public Object clone() throws CloneNotSupportedException {
        Lesson result = (Lesson) super.clone();
        result.room = room;
        result.subject = (Subject) subject.clone();
        return result;
    }

    public Lesson() {
        room = "";
        subject = new Subject();
    }

    public Lesson(int start, int end, @Nullable String room, @Nullable Subject subject) {
        super(start, end);
        this.room = room == null ? "" : room;
        this.subject = subject == null ? new Subject() : subject;
    }

    @NonNull
    public String getRoom() {
        return room;
    }

    public void setRoom(@Nullable String room) {
        this.room = room == null ? "" : room;
    }

    @NonNull
    public Subject getSubject() {
        return subject;
    }

    public void setSubject(@Nullable Subject subject) {
        this.subject = subject == null ? new Subject("", "", "") : subject;
    }

    @Override
    public int compareTo(Interval another) {
        int result =  super.compareTo(another);
        if(result != 0 || !(another instanceof Lesson)) return result;
        Lesson l = ((Lesson) another);
        result = room.compareTo(l.room);
        if(result != 0) return result;
        return subject.compareTo(l.subject);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (this == o) return true;

        try {
            Lesson lesson = (Lesson) o;
            if (!room.equals(lesson.room)) return false;
            return subject.equals(lesson.subject);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + room.hashCode();
        result = 31 * result + subject.hashCode();
        return result;
    }
}
