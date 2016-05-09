package com.byd.wsg.com.wsg.byd.plan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.byd.wsg.com.wsg.byd.plan.raw.RawDay;
import com.byd.wsg.com.wsg.byd.plan.raw.RawLesson;
import com.byd.wsg.com.wsg.byd.plan.raw.RawTimeTable;

import java.io.Serializable;
import java.sql.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by Jakub on 2016-04-21.
 */
public class TimeTable implements Iterable<DayTable>, Serializable, Cloneable {

    /**
     * this array is always sorted ascending (monday,tuestay, ... )
     */
    private ArrayList<DayTable> days = new ArrayList<>();

    public ArrayList<DayTable> getDaysClone() {
        return (ArrayList<DayTable>) days.clone();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        TimeTable result = new TimeTable();
        result.days = (ArrayList<DayTable>) days.clone();
        return result;
    }

    public TimeTable() {
    }

    public TimeTable(@Nullable Collection<DayTable> days) {
        if (days != null && !days.isEmpty()) {
            this.days.addAll(days);
            Collections.sort(this.days);
        }
    }

    public TimeTable(RawTimeTable rawTimeTable) throws ParseException {
        if (rawTimeTable != null && !rawTimeTable.days.isEmpty()) {
            Date from = rawTimeTable.getParsedFrom();
            Calendar c = Calendar.getInstance();
            c.clear();
            c.setTime(from);
            ArrayList<DayTable> dayTables = new ArrayList<>(rawTimeTable.days.size());
            for (RawDay day : rawTimeTable.days) {

                //SCALANIE LEKCJI
                List<RawLesson> lessons = day.getLessons();
                if (lessons.size() > 1) {
                    List<RawLesson> newLessons = new ArrayList<>();
                    for (int i = 0; i < lessons.size(); i++) {
                        RawLesson lesson = lessons.get(i);
                        if (lesson != null) {
                            for (int a = i + 1; a < lessons.size(); a++) {
                                RawLesson cur = lessons.get(a);
                                if (cur != null
                                        && cur.getType().equals(lesson.getType())
                                        && cur.getRoom().equals(lesson.getRoom())
                                        && cur.getTitle().equals(lesson.getTitle())
                                        && cur.getWith().equals(lesson.getWith())
                                        && cur.start <= lesson.end
                                        && cur.end >= lesson.start) {
                                    lesson.start = Math.min(lesson.start, cur.start);
                                    lesson.end = Math.max(lesson.end, cur.end);
                                    lessons.set(a, null);
                                }

                            }
                            newLessons.add(lesson);
                        }
                    }
                    lessons = newLessons;
                }

                // TWORZENIE INTERWALOW (LEKCJI) I SORTOWANIE
                ArrayList<Interval> intervals = new ArrayList<>(lessons.size());
                for (RawLesson lesson : lessons)
                    intervals.add(new Lesson(
                            lesson.getStart(),
                            lesson.getEnd(),
                            lesson.getRoom(),
                            new Subject(
                                    lesson.getTitle(),
                                    lesson.getWith(),
                                    lesson.getType())));
                Collections.sort(intervals);
                Log.d("LOG", "TimeTablexD: " + intervals.toString());
                dayTables.add(new DayTable(c, intervals));
                c = (Calendar) c.clone();
                c.add(Calendar.DAY_OF_MONTH, 1);
            }
            Collections.sort(dayTables);
            this.days = dayTables;
        } else this.days = new ArrayList<>();
    }

    public TimeTable(@Nullable DayTable[] days) {
        this(days == null ? null : Arrays.asList(days));
    }

    @Nullable
    public DayTable getFirstDayTable() {
        return isEmpty() ? null : days.get(0);
    }

    @Nullable
    public DayTable getLastDayTable() {
        return isEmpty() ? null : days.get(days.size() - 1);
    }

    public final boolean isEmpty() {
        return days.isEmpty();
    }

    public DayTable getDayTable(int index) {
        return days.get(index);
    }

    @Override
    @NonNull
    public Iterator<DayTable> iterator() {
        return days.iterator();
    }

    public int size() {
        return days.size();
    }


}
