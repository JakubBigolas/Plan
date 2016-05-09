package com.byd.wsg.com.wsg.byd.plan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Jakub on 2016-04-23.
 */
public class Subject implements Comparable<Subject>,Serializable, Cloneable {

    private String title, with, type;

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        Subject result = new Subject();
        result.title = title;
        result.with = with;
        result.type  = type;
        return result;
    }

    public Subject(){
        title = with = type = "";
    }

    public Subject(@Nullable String title,@Nullable String with,@Nullable String type) {
        this.title = title == null ? "" : title;
        this.with = with == null ? "" : with;
        this.type = type == null ? "" : type;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title == null ? "" : title;
    }

    @NonNull
    public String getWith() {
        return with;
    }

    public void setWith(@Nullable String with) {
        this.with = with == null ? "" : with;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type == null ? "" : type;
    }

    @Override
    public int compareTo(Subject another) {
        int result = title.compareTo(another.title);
        if (result != 0) return result;
        result = with.compareTo(another.with);
        if (result != 0) return result;
        return type.compareTo(another.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subject)) return false;

        Subject subject = (Subject) o;

        if (!title.equals(subject.title)) return false;
        if (!with.equals(subject.with)) return false;
        return type.equals(subject.type);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + with.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }


}
