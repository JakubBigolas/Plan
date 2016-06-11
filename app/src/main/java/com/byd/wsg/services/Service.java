package com.byd.wsg.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDoneException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Messenger;
import android.os.NetworkOnMainThreadException;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.telephony.ServiceState;
import android.util.Log;

import com.byd.wsg.com.wsg.byd.plan.DayTable;
import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.com.wsg.byd.plan.Interval;
import com.byd.wsg.com.wsg.byd.plan.News;
import com.byd.wsg.com.wsg.byd.plan.TimeTable;
import com.byd.wsg.com.wsg.byd.plan.raw.RawAuthenticate;
import com.byd.wsg.com.wsg.byd.plan.raw.RawStudent;
import com.byd.wsg.com.wsg.byd.plan.raw.RawTeacher;
import com.byd.wsg.com.wsg.byd.plan.raw.RawTimeTable;
import com.byd.wsg.com.wsg.byd.plan.raw.RawUser;
import com.byd.wsg.fragments.FragmentLogin;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.plany.R;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Jakub on 2016-05-22.
 */
public class Service extends android.app.Service {
    public static final String TAG = "LOG-Service";
    private String
            REST_URL = "http://wsg-mis.herokuapp.com/api/",
            LOGIN_TASK_NAME = "Servide.LoginTask",
            NEWS_TASK_NAME = "Service.NewsTask";

    UserToken userToken = new UserToken(null, null);

    Binder binder = new Binder();

    private RawUser user;
    private RawTeacher teacher;
    private RawStudent student;
    private News news;

    private ConcurrentHashMap<String, ServiceTask> tasks = new ConcurrentHashMap<>();

    public class Binder extends android.os.Binder {

        public boolean isNetworkAvalible() {
            return Service.this.isNetworkAvalible();
        }

        public TaskStatus getTaskStatus(String taskName) {
            ServiceTask task = tasks.get(taskName);
            return task == null ? null : task.getTaskStatus();
        }

        public Throwable getTaskError(String taskName) {
            ServiceTask task = tasks.get(taskName);
            return task == null ? null : task.getLastError();
        }

        public synchronized boolean startListenTask(String taskName, TaskListener taskListener) {
            return Service.this.startListenTask(taskName, taskListener);
        }

        public synchronized boolean stopListenTask(String taskName, TaskListener taskListener) {
            return Service.this.stopListenTask(taskName, taskListener);
        }

        public synchronized boolean startTaskTimeTable(String taskName, Calendar timeFrom, Calendar timeTo, int groupId, TaskListener taskListener) {
            return Service.this.startTaskTimeTable(taskName, timeFrom, timeTo, groupId, taskListener);
        }

        public synchronized boolean stopTaskTimeTable(String taskName) {
            return Service.this.stopTask(taskName);
        }

        public Throwable getLoginError() {
            return getTaskError(LOGIN_TASK_NAME);
        }

        public TaskStatus getLoginStatus() {
            return getTaskStatus(LOGIN_TASK_NAME);
        }

        public Throwable getNewsError() {
            return getTaskError(NEWS_TASK_NAME);
        }

        public TaskStatus getNewsStatus() {
            return getTaskStatus(NEWS_TASK_NAME);
        }

        public boolean setLoginListener(TaskListener taskListener) {
            return startListenTask(LOGIN_TASK_NAME, taskListener);
        }

        public boolean removeLoginListener(TaskListener taskListener) {
            return stopListenTask(LOGIN_TASK_NAME, taskListener);
        }

        public boolean setNewsListener(TaskListener taskListener) {
            return startListenTask(NEWS_TASK_NAME, taskListener);
        }

        public boolean removeNewsListener(TaskListener taskListener) {
            return stopListenTask(NEWS_TASK_NAME, taskListener);
        }

        public RawUser getUser() {
            return user;
        }

        public RawStudent getStudent() {
            return student;
        }

        public RawTeacher getTeacher() {
            return teacher;
        }

        public boolean login(String login, String password, TaskListener taskListener) {
            return Service.this.login(login, password, taskListener);
        }

        public void cancelLogin() {
            Service.this.cancelLogin();
        }

        public synchronized News getNews() {
            return news;
        }

        public synchronized void refreshNews(TaskListener taskListener) {
            Service.this.refreshNews(taskListener);
        }

        public void logout() {
            Service.this.logout();
        }

    }


    private class LoginTask extends ServiceTask {

        public LoginTask(TaskListener taskListener) {
            super(LOGIN_TASK_NAME, taskListener, userToken, REST_URL, Service.this.binder);
        }

        @Override
        protected Object doInBackground() {
            try {
                if (!isNetworkAvalible())
                    return TaskStatus.INTERNET_ACCES_ERROR;
                if ("".equals(getToken().getLogin())) return TaskStatus.AUTHENICATION_FAILED;
                if (isAborted()) return null;
                Retrofit retrofit = getREST();
                UserService userService = retrofit.create(UserService.class);
                if (isAborted()) return null;
                Call<RawUser> getUser = userService.getUser(getToken().getLogin());
                try {
                    if (isAborted()) return null;
                    Response<RawUser> response = executeWithToken(getUser);
                    if (isAborted()) return null;
                    Log.d(TAG, "doInBackground: " + response.isSuccessful() + " :::\n " + response.code() + ":::\n"
                            + response.message() + ":::\n" + response.errorBody() + ":::\n" + response.headers() + ":::\n" + response.body());
                    RawUser rawUser = response.body();
                    switch (response.code()) {
                        case 401:
                        case 403:
                            return TaskStatus.UNAUTHORIZED_ERROR;
                        case 408:
                            return TaskStatus.TIMEOUT;
                    }
                    if (rawUser == null) return TaskStatus.AUTHENICATION_FAILED;
                    user = rawUser;
                    user.setPassword(userToken.getPassword());
                } catch (IOException e) {
                    setLastError(e);
                    return TaskStatus.IO_ERROR;
                }

                if (isAborted()) return null;
                Call<RawTeacher[]> getTechers = userService.getTeachers();
                try {
                    if (isAborted()) return null;
                    Response<RawTeacher[]> response = executeWithToken(getTechers);
                    if (isAborted()) return null;
                    Log.d(TAG, "doInBackground: " + response.isSuccessful() + " :::\n " + response.code() + ":::\n"
                            + response.message() + ":::\n" + response.errorBody() + ":::\n" + response.headers() + ":::\n" + response.body());
                    RawTeacher[] rawTeachers = response.body();
                    switch (response.code()) {
                        case 401:
                        case 403:
                            return TaskStatus.UNAUTHORIZED_ERROR;
                        case 408:
                            return TaskStatus.TIMEOUT;
                    }
                    if (rawTeachers != null)
                        for (RawTeacher teacher : rawTeachers) {
                            if (teacher.getUser() != null && Long.compare(teacher.getUser().getId(), user.getId()) == 0)
                                Service.this.teacher = teacher;
                            // TODO MOZNA BY ZROBIC ZEBY POBIERALO OD RAZU PO LOGINIE (BO PO ID JEDNAK SIE NIE DA) ALE NIE MA TEGO W REST
                            // TODO POZA TYM CO STOI NA PRZESZKODZE ZEBY BYC KILKOMA NAUCZYCIELAMI NA RAZ xD
                            Log.d(TAG, "doInBackground: " + teacher);
                        }
                } catch (IOException e) {
                    setLastError(e);
                    return TaskStatus.IO_ERROR;
                }

                if (isAborted()) return null;
                Call<RawStudent[]> getStudents = userService.getStudents();
                try {
                    if (isAborted()) return null;
                    Response<RawStudent[]> response = executeWithToken(getStudents);
                    if (isAborted()) return null;
                    Log.d(TAG, "doInBackground: " + response.isSuccessful() + " :::\n " + response.code() + ":::\n"
                            + response.message() + ":::\n" + response.errorBody() + ":::\n" + response.headers() + ":::\n" + response.body());
                    RawStudent[] rawStudents = response.body();
                    switch (response.code()) {
                        case 401:
                        case 403:
                            return TaskStatus.UNAUTHORIZED_ERROR;
                        case 408:
                            return TaskStatus.TIMEOUT;
                    }
                    if (rawStudents != null)
                        for (RawStudent student : rawStudents) {
                            if (student.getUser() != null && Long.compare(student.getUser().getId(), user.getId()) == 0)
                                Service.this.student = student;
                            // TODO MOZNA BY ZROBIC ZEBY POBIERALO OD RAZU PO LOGINIE (BO PO ID JEDNAK SIE NIE DA) ALE NIE MA TEGO W REST
                            // TODO POZA TYM CO STOI NA PRZESZKODZE ZEBY BYC KILKOMA STUDENTAMI NA RAZ xD
                            Log.d(TAG, "doInBackground: " + student);
                        }
                } catch (IOException e) {
                    setLastError(e);
                    return TaskStatus.IO_ERROR;
                }

            } catch (Throwable t) {
                Log.d(TAG, "doInBackground: " + t.getLocalizedMessage());
                setLastError(t);
                return TaskStatus.UNEXPECTED_ERROR;
            }
            return TaskStatus.DONE;
        }
    }

    public synchronized void logout() {
        for (ServiceTask task : tasks.values())
            if (task != null)
                task.abort();
        tasks.clear();
        userToken = new UserToken("", "");
        teacher = null;
        student = null;
        user = null;
        news = null;
    }

    public synchronized boolean login(String login, String password, TaskListener taskListener) {
        ServiceTask loginTask = tasks.get(LOGIN_TASK_NAME);
        if (loginTask != null && loginTask.isRunning())
            return false;
        for (ServiceTask task : tasks.values())
            if (task != null)
                task.abort();
        tasks.clear();
        userToken = new UserToken(login, password);
        loginTask = new LoginTask(taskListener);
        tasks.put(LOGIN_TASK_NAME, loginTask);
        loginTask.execute();
        return true;
    }

    public synchronized void cancelLogin() {
        stopTask(LOGIN_TASK_NAME);
    }

    public boolean isNetworkAvalible() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    class TimeTableTask extends ServiceTask {

        private Calendar timeFrom, timeTo;

        private int groupId;

        private RawTimeTable rawTimeTable;

        public RawTimeTable getRawTimeTable() {
            return rawTimeTable;
        }

        public void setRawTimeTable(RawTimeTable rawTimeTable) {
            this.rawTimeTable = rawTimeTable;
        }

        public TimeTableTask(String taskName, Calendar timeFrom, Calendar timeTo, int groupId, TaskListener taskListener) {
            super(taskName, taskListener, userToken, REST_URL, Service.this.binder);
            this.timeFrom = (Calendar) timeFrom.clone();
            this.timeTo = (Calendar) timeTo.clone();
            this.groupId = groupId;
        }

        @Override
        protected Object doInBackground() {
            if (!isNetworkAvalible())
                return TaskStatus.INTERNET_ACCES_ERROR;
            if (timeFrom == null || timeTo == null)
                return TaskStatus.UNEXPECTED_ERROR;

            RequestTimeTable requestTimeTable = new RequestTimeTable();
            requestTimeTable.setFrom(timeFrom);
            requestTimeTable.setTo(timeTo);

            StudentGroupService service = getREST().create(StudentGroupService.class);
            Call<RawTimeTable> getTimeTable = service.getTimeTable(groupId, requestTimeTable);

            try {
                if (isAborted()) return null;
                Response<RawTimeTable> response = executeWithToken(getTimeTable);
                if (isAborted()) return null;
                Log.d(TAG, "doInBackground: " + response.isSuccessful() + " :::\n " + response.code() + ":::\n"
                        + response.message() + ":::\n" + response.errorBody() + ":::\n" + response.headers() + ":::\n" + response.body());
                switch (response.code()) {
                    case 401:
                    case 403:
                        return TaskStatus.UNAUTHORIZED_ERROR;
                    case 408:
                        return TaskStatus.TIMEOUT;
                }
                rawTimeTable = response.body();

                refreshNews(null);

            } catch (IOException e) {
                setLastError(e);
                return TaskStatus.IO_ERROR;
            }
            return TaskStatus.DONE;
        }
    }


    public synchronized boolean startTaskTimeTable(String taskName, Calendar timeFrom, Calendar timeTo, int groupId, TaskListener taskListener) {
        if (!tasks.containsKey(taskName))
            return false;
        ServiceTask task = new TimeTableTask(taskName, timeFrom, timeTo, groupId, taskListener);
        tasks.put(taskName, task);
        task.execute();
        return true;
    }

    public synchronized boolean startListenTask(String taskName, TaskListener taskListener) {
        if (!tasks.containsKey(taskName))
            return false;
        ServiceTask task = tasks.get(taskName);
        return task.setTaskListener(taskListener);
    }

    public synchronized boolean stopListenTask(String taskName, TaskListener taskListener) {
        if (!tasks.containsKey(taskName))
            return false;
        ServiceTask task = tasks.get(taskName);
        return task.removeTaskListener(taskListener);
    }

    public synchronized boolean stopTask(String taskName) {
        if (!tasks.containsKey(taskName))
            return false;
        ServiceTask task = tasks.get(taskName);
        task.abort();
        return true;
    }

    private class NewsTask extends ServiceTask {

        public NewsTask() {
            super(NEWS_TASK_NAME, null, userToken, REST_URL, binder);
        }

        @Override
        protected Object doInBackground() {
            Service.this.news = null;
            if (!isNetworkAvalible())
                return TaskStatus.INTERNET_ACCES_ERROR;
            if (student == null || student.getStudentGroup() == null || student.getStudentGroup().getId() == null)
                return TaskStatus.UNEXPECTED_ERROR;
            if (isAborted()) return null;
            Calendar timeFrom = resetTime(Calendar.getInstance());
            timeFrom.add(Calendar.DAY_OF_MONTH, -(getConvertedDay(timeFrom) - 1));
            Calendar timeTo = resetTimeToEnd(Calendar.getInstance());
            timeTo.set(timeTo.get(Calendar.YEAR), timeTo.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
            timeTo.add(Calendar.DAY_OF_MONTH, 7 - (getConvertedDay(timeTo)));
            if (isAborted()) return null;


            TimeTableTask timeTableTask = new TimeTableTask(NEWS_TASK_NAME + ".timeTableTask", timeFrom, timeTo, student.getStudentGroup().getId().intValue(), null);
            timeTableTask.run();
            if (isAborted()) return null;
            if (timeTableTask.getRawTimeTable() == null)
                return TaskStatus.UNEXPECTED_ERROR;

            try {
                Event.Helper helper = new Event.Helper(new SQLiteHelper(getApplicationContext()), Event.Helper.TABLE_NAME);
                TimeTable timeTable = new TimeTable(timeTableTask.getRawTimeTable());
                Calendar today = Calendar.getInstance();
                ArrayList<Event> events = null;
                if (isAborted()) return null;

                Calendar startOftoday = resetTime(Calendar.getInstance());
                Calendar endOftoday = resetTimeToEnd(Calendar.getInstance());
                DayTable thisDayTable = timeTable.getDayTable(startOftoday);
                News.PeriodOfTime thisDay = new News.PeriodOfTime();
                if (thisDayTable != null)
                    for (Interval interval : thisDayTable) {
                        if (interval.getType() == Interval.Type.LESSON) {
                            thisDay.setCountOfLessons(thisDay.getCountOfLessons() + 1);
                            Long currTimeLesson = thisDayTable.getDate().getTimeInMillis() + (interval.getStart() * 60000);
                            Long currTime = today.getTimeInMillis();
                            if (currTime >= currTimeLesson)
                                thisDay.setCurrentCountOfLessons(thisDay.getCurrentCountOfLessons() + 1);
                        }
                    }
                if (isAborted()) return null;
                events = helper.read(user.getLogin(), startOftoday, true, endOftoday, true, null, null);
                thisDay.setEvents(events);
                thisDay.setStartTime(startOftoday);
                thisDay.setEndTime(endOftoday);
                for (Event event : events) {
                        thisDay.setCountOfEvents(thisDay.getCountOfEvents() + 1);
                        Long currTime = today.getTimeInMillis();
                        if (currTime >= event.getTime().getTimeInMillis())
                            thisDay.setCurrentCountOfEnvents(thisDay.getCurrentCountOfEnvents() + 1);
                }

                if (isAborted()) return null;
                Calendar weekBegin = resetTime(Calendar.getInstance());
                Calendar weekEnd = resetTimeToEnd(Calendar.getInstance());
                weekBegin.add(Calendar.DAY_OF_MONTH, -(getConvertedDay(weekBegin) - 1));
                weekEnd.add(Calendar.DAY_OF_MONTH, 7 - (getConvertedDay(weekEnd)));
                ArrayList<DayTable> thisWeekDays = timeTable.getDays(weekBegin, weekEnd);
                News.PeriodOfTime thisWeek = new News.PeriodOfTime();
                if (thisWeekDays != null)
                    for (DayTable dayTable : thisWeekDays)
                        for (Interval interval : dayTable) {
                            if (interval.getType() == Interval.Type.LESSON) {
                                thisWeek.setCountOfLessons(thisWeek.getCountOfLessons() + 1);
                                Long currTimeLesson = dayTable.getDate().getTimeInMillis() + (interval.getStart() * 60000);
                                Long currTime = today.getTimeInMillis();
                                if (currTime >= currTimeLesson)
                                    thisWeek.setCurrentCountOfLessons(thisWeek.getCurrentCountOfLessons() + 1);
                            }
                        }
                if (isAborted()) return null;
                events = helper.read(user.getLogin(), weekBegin, true, weekEnd, true, null, null);
                thisWeek.setEvents(events);
                thisWeek.setStartTime(weekBegin);
                thisWeek.setEndTime(weekEnd);
                for (Event event : events) {
                    thisWeek.setCountOfEvents(thisWeek.getCountOfEvents() + 1);
                    Long currTime = today.getTimeInMillis();
                    if (currTime >= event.getTime().getTimeInMillis())
                        thisWeek.setCurrentCountOfEnvents(thisWeek.getCurrentCountOfEnvents() + 1);
                }

                if (isAborted()) return null;
                Calendar monthBegin = resetTime(Calendar.getInstance());
                Calendar monthEnd = resetTimeToEnd(Calendar.getInstance());
                monthBegin.set(Calendar.DAY_OF_MONTH, 1);
                monthEnd.set(monthEnd.get(Calendar.YEAR), monthBegin.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
                ArrayList<DayTable> thisMonthDays = timeTable.getDays(monthBegin, monthEnd);
                News.PeriodOfTime thisMonth = new News.PeriodOfTime();
                if (thisMonthDays != null)
                    for (DayTable dayTable : thisMonthDays)
                        for (Interval interval : dayTable) {
                            if (interval.getType() == Interval.Type.LESSON) {
                                thisMonth.setCountOfLessons(thisMonth.getCountOfLessons() + 1);
                                Long currTimeLesson = dayTable.getDate().getTimeInMillis() + (interval.getStart() * 60000);
                                Long currTime = today.getTimeInMillis();
                                if (currTime >= currTimeLesson)
                                    thisMonth.setCurrentCountOfLessons(thisMonth.getCurrentCountOfLessons() + 1);
                            }
                        }
                if (isAborted()) return null;
                events = helper.read(user.getLogin(), monthBegin, true, monthEnd, true, null, null);
                thisMonth.setEvents(events);
                thisMonth.setStartTime(monthBegin);
                thisMonth.setEndTime(monthEnd);
                for (Event event : events) {
                    thisMonth.setCountOfEvents(thisMonth.getCountOfEvents() + 1);
                    Long currTime = today.getTimeInMillis();
                    if (currTime >= event.getTime().getTimeInMillis())
                        thisMonth.setCurrentCountOfEnvents(thisMonth.getCurrentCountOfEnvents() + 1);
                }

                if (isAborted()) return null;
                news = new News(timeTable, thisDay, thisWeek, thisMonth);
            } catch (Throwable e) {
                Log.d(TAG, "doInBackground: " + e.getLocalizedMessage());
                return TaskStatus.UNEXPECTED_ERROR;
            }
            return TaskStatus.DONE;
        }
    }



    private Calendar resetTimeToEnd(Calendar c){
        c = resetTime(c);
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.SECOND, -1);
        return c;
    }

    private Calendar resetTime(Calendar c){
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    private int getConvertedDay(Calendar c) {
        int day = c.get(Calendar.DAY_OF_WEEK);
        return day == 1 ? 7 : day - 1;
    }

    public void refreshNews(TaskListener taskListener) {
        ServiceTask newsTask = tasks.get(NEWS_TASK_NAME);
        if (newsTask != null && newsTask.isRunning()) return;
        newsTask = new NewsTask();
        newsTask.setTaskListener(taskListener);
        tasks.put(NEWS_TASK_NAME, newsTask);
        newsTask.execute();
    }

    ////////////////////////////////////// SERWIS JAKO TAKI ///////////////////////////////////////////////////////

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: " + (binder != null));
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    int ONGOING_NOTIFICATION_ID = 123;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_date_range_white_24dp);
//        builder.setContentText("content text");
        builder.setContentTitle(getString(R.string.service_notif_title));
        Notification n = builder.build();


//        Notification notification = new Notification(R.drawable.ic_delete_white_24dp, "This is an example text.",
//                System.currentTimeMillis());
//        Intent notificationIntent = new Intent(this, Service.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//        notification.setLatestEventInfo(this, getText(R.string.notification_title),
//                getText(R.string.notification_message), pendingIntent);
        startForeground(ONGOING_NOTIFICATION_ID, n);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
}
