package com.byd.wsg.com.wsg.byd.plan.raw;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub on 2016-04-25.
 */
public class RawDay {

    // TODO DO ZAMAPOWANIA POD RETROFITA

    String title;

    List<RawLesson> lessons;

    public RawDay(String title, List<RawLesson> lessons) {
        this.title = title;
        this.lessons = lessons;
    }

    @NonNull
    public String getTitle() {
        return title == null ? "" : title;
    }

    @NonNull
    public List<RawLesson> getLessons() {
        return lessons == null ? new ArrayList<RawLesson>() : lessons;
    }

    @Override
    public String toString() {
        return "RawDay{" +
                "title='" + title + '\'' +
                ", lessons=" + lessons +
                '}';
    }
}
