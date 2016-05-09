package com.byd.wsg.com.wsg.byd.plan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Jakub on 2016-04-21.
 */
public class DayTable implements Comparable<DayTable>, Serializable, Cloneable, Iterable<Interval>{

    /**
     * Always sorted
     */
    @NonNull
    private ArrayList<Interval> intervals = new ArrayList<>();

    @NonNull
    private Calendar date;

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        DayTable result = new DayTable();
        result.intervals = (ArrayList<Interval>) intervals.clone();
        result.date = (Calendar) date.clone();
        return result;
    }

    public DayTable(){
        date = Calendar.getInstance();
    }

    public int size(){
        return intervals.size();
    }

    public Interval getInterval(int index){
        return intervals.get(index);
    }

    public Lesson getLesson(int index){
      try {
          return (Lesson) intervals.get(index);
      }catch (ClassCastException e){
          return null;
      }
    }

    public ArrayList<Interval> getIntervalsClone(){
        return (ArrayList<Interval>) intervals.clone();
    }

    /**
     * @param intervals collection of lessons
     * @param date    if null then will be set default instance
     */
    public DayTable(Calendar date,@Nullable Collection<Interval> intervals) {
        if(intervals != null && !intervals.isEmpty()){
            this.intervals.addAll(intervals);
            Collections.sort(this.intervals);
        }
        this.date = date == null ? Calendar.getInstance() : date;
    }

    /**
     * @param intervals collection of lessons
     * @param date    if null then will be set default instance
     */
    public DayTable(Calendar date,@Nullable Interval[] intervals) {
        this(date, intervals == null ? null : Arrays.asList(intervals));
    }

    public DayTable(Calendar date){
        this.date = date == null ? Calendar.getInstance() : date;
    }

    private static final SimpleDateFormat
            monthDateFormat = new SimpleDateFormat("mmmm"),
            dayDateFormat = new SimpleDateFormat("EEE"),
            fullDateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public Calendar getDate() {
        return (Calendar) date.clone();
    }

    public String getDateLocaleString() {
        return fullDateFormat.format(date.getTime());
    }

    public String getDayLocaleString(){
        return dayDateFormat.format(date.getTime());
    }

    public int getYear() {
        return date.get(Calendar.YEAR);
    }

    public int getMonth() {
        return date.get(Calendar.MONTH);
    }

    public String getMonthLocaleString() {
        return monthDateFormat.format(date.getTime());
    }

    public int getDay() {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    public int getDayOfWeek() {
        return date.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfYear() {
        return date.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public int compareTo(DayTable another) {
        return date.compareTo(another.date);
    }

    public boolean isEmpty(){
        return intervals.isEmpty();
    }

    @Override
    public Iterator<Interval> iterator() {
        return intervals.iterator();
    }

    public ListIterator<Interval> listIterator(){
        return intervals.listIterator();
    }
}
