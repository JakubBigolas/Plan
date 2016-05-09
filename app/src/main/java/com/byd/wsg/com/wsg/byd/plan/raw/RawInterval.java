package com.byd.wsg.com.wsg.byd.plan.raw;

import android.support.annotation.NonNull;

import com.byd.wsg.com.wsg.byd.plan.Interval;

/**
 * Created by Jakub on 2016-04-25.
 */
public class RawInterval {

    public int start;

    public int end;

    public String type;

    public RawInterval(int start, int end, String type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @NonNull
    public String getType() {
        return type;
    }

    /**
     * @return LESSON if type is null
     * @throws IllegalArgumentException if type does not match to any of static field of Intevals.Type
     */
    @NonNull
    public Interval.Type getTypeEnum() throws IllegalArgumentException{
        return type == null ? Interval.Type.LESSON : Interval.Type.valueOf(type.toUpperCase());
    }


    @Override
    public String toString() {
        return "RawInterval{" +
                "start=" + start +
                ", end=" + end +
                ", type='" + type + '\'' +
                '}';
    }
}

