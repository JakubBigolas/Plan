package com.byd.wsg.services;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.byd.wsg.com.wsg.byd.plan.raw.RawAuthenticate;
import com.byd.wsg.com.wsg.byd.plan.raw.RawUser;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jakub on 2016-06-05.
 */
public abstract class ServiceTask extends Thread {

    private static final String TAG = "LOG-ServiceTask";

    private String REST_URL;

    private OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private UserToken token;

    private boolean aborted;

    private TaskListener taskListener;

    private String taskName;

    private Throwable lastError;

    private TaskStatus taskStatus;

    private Service.Binder binder;

    public UserToken getToken() {
        return token;
    }

    public ServiceTask(String taskName, TaskListener taskListener, UserToken userToken, String restUrl, Service.Binder binder) {
        this.token = userToken;
        this.REST_URL = restUrl;
        this.taskName = taskName;
        this.taskListener = taskListener;
        this.binder = binder;
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                Request request = null;
                if (token.getToken() != null) {
                    request = original.newBuilder()
                            .headers(original.headers())
                            .addHeader("X-Auth-Token", token.getToken().getToken())
                            .method(original.method(), original.body())
                            .build();
                } else {
                    request = original.newBuilder()
                            .method(original.method(), original.body())
                            .build();
                }
                return chain.proceed(request);
            }
        });
    }

    private synchronized void setTaskStatus(TaskStatus taskStatus, Throwable exception) {
        if (taskStatus == TaskStatus.CANCELLED) {
            lastError = null;
            return;
        }
        this.taskStatus = taskStatus;
        this.lastError = exception;
        if (taskListener != null)
            taskListener.sendEvent(taskStatus, exception, binder);
    }

    public Throwable getLastError() {
        return lastError;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public String getTaskName() {
        return taskName;
    }

    public TaskListener getTaskListener() {
        return taskListener;
    }

    public synchronized boolean setTaskListener(TaskListener taskListener) {
        if (this.taskListener != null) return false;
        this.taskListener = taskListener;
        return true;
    }

    public synchronized boolean removeTaskListener(TaskListener taskListener) {
        if (taskListener != this.taskListener) return false;
        this.taskListener = null;
        return true;
    }


    Call call;

    boolean ignoreExceptions = false;

    public boolean isIgnoreExceptions() {
        return ignoreExceptions;
    }

    public void setIgnoreExceptions(boolean ignoreExceptions) {
        this.ignoreExceptions = ignoreExceptions;
    }

    private Handler handler;

    public boolean isRunning = false;


    @Override
    public final void run() {
        try {
            isRunning = true;
            taskStatus = TaskStatus.STARTED;
            onPostExecute(doInBackground());
            isRunning = false;
        } catch (Throwable t) {
            Log.d(TAG, "handleMessage: " + t.getLocalizedMessage());
            isRunning = false;
            if (!ignoreExceptions) throw t;
        }
    }

    protected abstract Object doInBackground();

    protected void onPostExecute(Object o) {
        if (!isAborted()) setTaskStatus((TaskStatus) o, lastError);
        else setTaskStatus(TaskStatus.CANCELLED, null);
    }

    public boolean isFinished() {
        return taskStatus != null && taskStatus != TaskStatus.STARTED;
    }

    public boolean isRunning(){
        return isRunning;
    }

    protected final void setLastError(Throwable t) {
        lastError = t;
    }

    /**
     * @param call
     * @return if cancelled return null
     * @throws IOException
     */
    public Response executeWithToken(Call call) throws IOException {
        Log.d(TAG, "executeWithToken: " + call.request().url().toString());
        if (call != null) {
            Log.d(TAG, "executeWithToken: " + call.request().headers().toString());
        }

        Call temp = call.clone();
        if (isAborted()) return null;
        setCall(call);
        if (isAborted()) return null;
        Response response = call.execute();
        if (isAborted()) return null;
        Log.d(TAG, "executeWithToken: code" + response.code());

        if (response.code() != 401) return response;// UNAUTHORIZED NOT OCCURED
        Call<RawAuthenticate> getToken = getREST().create(UserService.class).getToken(token.getLogin(), token.getPassword());

        if (isAborted()) return null;
        setCall(getToken);
        if (isAborted()) return null;
        Response<RawAuthenticate> respToken = getToken.execute();
        if (isAborted()) return null;
        if (respToken.body() != null) token.setToken(respToken.body());
        Log.d(TAG, "executeWithToken-2: code" + respToken.code());

        if (isAborted()) return null;
        setCall(temp);
        if (isAborted()) return null;
        response = temp.execute();
        if (isAborted()) return null;
        this.call = null;
        Log.d(TAG, "executeWithToken-3: code" + respToken.code());
        return response;
    }

    public Retrofit getREST() {
        return new Retrofit.Builder()
                .baseUrl(REST_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private synchronized void setCall(Call call) {
        this.call = call;
    }

    public synchronized final boolean isAborted() {
        return aborted;
    }

    public synchronized final void abort() {
        try {
            if (call != null) {
                call.cancel();
                call = null;
            }
        } catch (Throwable t) {
            Log.d(TAG, "abort: " + t.getLocalizedMessage());
        }
        aborted = true;
    }

    public void execute() {
        start();
    }

}