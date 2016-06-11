package com.byd.wsg.services;

import android.os.Handler;
import android.os.Message;

/**
 * Created by Jakub on 2016-06-06.
 */
public abstract class TaskListener extends Handler {
    public abstract void onEvent(TaskStatus taskStatus, Throwable error, Service.Binder service);

    @Override
    public final void handleMessage(Message msg) {
        Object[] args = (Object[]) msg.obj;
        TaskStatus taskStatus = (TaskStatus) args[0];
        Throwable error = (Throwable) args[1];
        Service.Binder service = (Service.Binder) args[2];
        this.onEvent(taskStatus, error, service);
    }

    public final void sendEvent(TaskStatus taskStatus, Throwable error, Service.Binder service) {
        Object[] args = new Object[]{taskStatus, error, service};
        Message msg = new Message();
        msg.obj = args;
        this.sendMessage(msg);
    }
}