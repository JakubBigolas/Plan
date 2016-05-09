package com.byd.wsg.com.wsg.byd.plan.raw;

import com.byd.wsg.com.wsg.byd.plan.Interval;

import java.util.List;

/**
 * Created by Jakub on 2016-04-25.
 */
public class RawData {

    public List<RawInterval> intervals;

    public RawTimeTable timetable;

    @Override
    public String toString() {
        return "RawData{" +
                "intervals=" + intervals +
                ", timetable=" + timetable +
                '}';

    }
}
