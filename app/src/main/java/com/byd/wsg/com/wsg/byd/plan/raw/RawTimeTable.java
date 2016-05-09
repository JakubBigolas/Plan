package com.byd.wsg.com.wsg.byd.plan.raw;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Jakub on 2016-04-25.
 */
public class RawTimeTable {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public String from;

    public String to;

    public List<RawDay> days;

    public RawTimeTable(String from, String to, List<RawDay> days) {
        this.from = from;
        this.to = to;
        this.days = days;
    }

    /**
     * @return current time if from is null
     */
    @NonNull
    public String getFrom() {
        return from != null ? from : dateFormat.format(Calendar.getInstance().getTime());
    }

    /**
     * @return current time if to is null
     */
    @NonNull
    public String getTo() {
        return to != null ? to : dateFormat.format(Calendar.getInstance().getTime());
    }

    /**
     * @return current time if from is null
     * @throws ParseException
     */
    @NonNull
    public Date getParsedFrom() throws ParseException {
        if (from == null) return Calendar.getInstance().getTime();
        return dateFormat.parse(from);
    }

    /**
     * @return current time if to is null
     * @throws ParseException
     */
    @NonNull
    public Date getParsedTo() throws ParseException {
        if (to == null) return Calendar.getInstance().getTime();
        return dateFormat.parse(to);
    }

    @NonNull
    public List<RawDay> getDays() {
        return days == null ? new ArrayList<RawDay>() : days;
    }

    @Override
    public String toString() {
        return "RawTimeTable{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", days=" + days +
                '}';
    }
}
