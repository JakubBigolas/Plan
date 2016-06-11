package com.byd.wsg.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Jakub on 2016-06-06.
 */
public class RequestTimeTable {

    private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public String from;

    public String to;

    public void setFrom(Calendar from) {
        this.from = df.format(from.getTime());
    }

    public void setTo(Calendar to) {
        this.to = df.format(to.getTime());
    }


}
