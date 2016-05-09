package com.byd.wsg.com.wsg.byd.plan;

import android.support.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Jakub on 2016-04-25.
 */
public class Interval implements Serializable, Cloneable, Comparable<Interval> {
    private int
            start,
            end;
    Type type = Type.LESSON;

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        Interval result = new Interval();
        result.start = start;
        result.end = end;
        return result;
    }

    public static enum Type {
        BREAK, LESSON, FREE;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    public static final int
            FIRST_MINUTE_OF_DAY = 0,
            LAST_MINUTE_OF_DAY = 1440;

    public Interval() {
    }

    /**
     * @param type - if null default Type.LESSON
     */
    public Interval(int start, int end, Type type) {
        if (start < FIRST_MINUTE_OF_DAY)
            throw new RuntimeException("Start time cannot be lower than 0");
        if (start > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        if (end < start) throw new RuntimeException("End time cannot be lower than start time");
        if (end > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        this.start = start;
        this.end = end;
        this.type = type == null ? Type.LESSON : type;
    }


    /**
     * Type is set Type.LESSON
     */
    public Interval(int start, int end) {
        if (start < FIRST_MINUTE_OF_DAY)
            throw new RuntimeException("Start time cannot be lower than 0");
        if (start > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        if (end < start) throw new RuntimeException("End time cannot be lower than start time");
        if (end > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        this.start = start;
        this.end = end;
    }

    public void setStartAndEnd(int start, int end) {
        if (start < FIRST_MINUTE_OF_DAY)
            throw new RuntimeException("Start time cannot be lower than 0");
        if (start > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        if (end < start) throw new RuntimeException("End time cannot be lower than start time");
        if (end > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        this.start = start;
        this.end = end;
        this.start = start;
        this.end = end;
    }

    @NonNull
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        if (start < FIRST_MINUTE_OF_DAY)
            throw new RuntimeException("Start time cannot be lower than 0");
        if (start > end) throw new RuntimeException("Start time cannot be higher than end time");
        if (start > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        this.start = start;
    }

    @NonNull
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        if (end < start) throw new RuntimeException("End time cannot be lower than start time");
        if (end > LAST_MINUTE_OF_DAY)
            throw new RuntimeException("End time must by in range of one day (0 - " + LAST_MINUTE_OF_DAY + ")");
        this.end = end;
    }

    @Override
    public int compareTo(Interval another) {
        int result = Integer.compare(this.start, another.start);
        if (result != 0) return result;
        result = Integer.compare(this.end, another.end);
        if (result != 0) return result;
        return type.compareTo(another.type);

    }

    @NonNull
    public Type getType() {
        return type;
    }


    public void setType(@NonNull Type type) {
        this.type = type == null ? Type.LESSON : type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        try {
            Interval interval = (Interval) o;
            if (start != interval.start) return false;
            if (end != interval.end) return false;
            return type == interval.type;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        result = 31 * result + type.hashCode();
        return result;
    }
}
