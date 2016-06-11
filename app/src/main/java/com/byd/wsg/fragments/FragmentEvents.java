package com.byd.wsg.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;

import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.com.wsg.byd.plan.TimeTable;
import com.byd.wsg.example.ExampleData;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;
import com.byd.wsg.services.Service;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Jakub on 2016-05-14.
 */
public class FragmentEvents extends Fragment implements OnComponentListener, ServiceConnection {

    private View v, statusBarLayout;
    private ViewPager pager;
    private Toolbar toolbar;
    private MyPagerAdapter adapter;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private AppBarLayout appBarLayout;
    private Calendar startTime, endTime;
    private TextView pageTextView;

    private String username;
    private ArrayList<Event> events;

    public static final String
            TAG = "LOG-FragmentEvents",
            SET_TIME_START = "FragmentEvents.SET_TIME_START",
            SET_TIME_END = "FragmentEvents.SET_TIME_END",
            SET_USERNAME = "FragmentEvents.SET_USERNAME";


    private Service.Binder service;

    private static class QueueItem {
        Object o1, o2, o3;

        public QueueItem(Object o1, Object o2, Object o3) {
            this.o1 = o1;
            this.o2 = o2;
            this.o3 = o3;
        }
    }

    ArrayList<QueueItem> queueItems = new ArrayList<>();

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: ");
        this.service = (Service.Binder) service;
        if (((Service.Binder) service).getNews() != null) {
            Calendar timeFrom = resetTime(Calendar.getInstance());
            timeFrom.add(Calendar.DAY_OF_MONTH, -(getConvertedDay(timeFrom) - 1));
            Calendar timeTo = resetTimeToEnd(Calendar.getInstance());
            timeTo.set(timeTo.get(Calendar.YEAR), timeTo.get(Calendar.MONTH) + 1, 1, 0, 0, 0);
            Event.Helper helper = new Event.Helper(new SQLiteHelper(getActivity()), Event.Helper.TABLE_NAME);
            username = ((Service.Binder) service).getUser().getLogin();
            events = helper.read(username, timeFrom, true, timeTo, true, null, null);
        }

        if (adapter == null) adapter = new MyPagerAdapter(getChildFragmentManager());
        if (v != null)
            pager.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        synchronized (queueItems) {
            for (QueueItem q : queueItems)  // BO SIE CHAMSKI ANDROID UPARL ZE NIE ODSIWEZY I CH....J
                onComponentEvent(q.o1, q.o2, q.o3);
            queueItems.clear();
        }

    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart:");
        Intent intent = new Intent(getActivity(), Service.class);
        getAppCompatActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        try {
            if (service != null) {
//                service.removeNewsListener(newsListener);
                getAppCompatActivity().getApplicationContext().unbindService(this);
            }
        } catch (Throwable t) {
            Log.d(TAG, "onStop: " + t.getLocalizedMessage());
        }
        service = null;
        super.onStop();
    }


    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        this.service = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_events, menu);
        menu.findItem(R.id.delete).setVisible(events != null && !events.isEmpty());
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.fragment_events_toolbar_title);
//        actionBar.setHomeButtonEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                int page = pager.getCurrentItem();
                Event event = events.get(page);
                Event.Helper helper = new Event.Helper(new SQLiteHelper(getActivity()), Event.Helper.TABLE_NAME);
                if (event.getId() == Event.ID_NOT_STORED || helper.delete(event)) {
                    events.remove(pager.getCurrentItem());
                    adapter.notifyDataSetChanged();
                    pager.setAdapter(adapter);
                    pager.setCurrentItem(page, false);
                    item.setVisible(!events.isEmpty());
                    Tools.toast(getActivity(), getString(R.string.fragment_events_deleted_correctly));
                } else
                    Tools.toast(getActivity(), getString(R.string.fragment_events_deleted_incorrectly));
                break;
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_events, null, false);

        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        statusBarLayout = v.findViewById(R.id.statusBarLayout);
        pager = (ViewPager) v.findViewById(R.id.viewPager);
        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.coordinatorLayout);
        fab = (FloatingActionButton) v.findViewById(R.id.fab);
        appBarLayout = (AppBarLayout) v.findViewById(R.id.appBarLayout);
        pageTextView = (TextView) v.findViewById(R.id.pageTextView);

        return v;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null)
            state = getArguments();
        setHasOptionsMenu(true);
        if (state != null) {
            username = state.getString(SET_USERNAME, username);
            startTime = (Calendar) Tools.loadDeserializedObjectLogged(state, SET_TIME_START, null, TAG);
            endTime = (Calendar) Tools.loadDeserializedObjectLogged(state, SET_TIME_END, null, TAG);
        }
    }

    public AppCompatActivity getAppComatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        statusBarLayout.bringToFront();
        getAppComatActivity().setSupportActionBar(toolbar);
//        adapter = new MyPagerAdapter(getChildFragmentManager());
//        adapter.notifyDataSetChanged();
//        pager.setAdapter(adapter);
//        if (state != null) pager.setCurrentItem(state.getInt("pager.currentItem", 0));
        statusBarLayout.bringToFront();
        setHasOptionsMenu(true);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0)
                    fab.show();
                else if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    fab.hide();
                }
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar time = null;
                if (events != null && !events.isEmpty()) {
                    time = (Calendar) events.get(events.size() - 1).getTime().clone();
                    time.add(Calendar.HOUR, 1);
                } else
                    time = startTime != null ? startTime : endTime != null ? endTime : Tools.prepareDate(System.currentTimeMillis());
                CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                        .setFirstDayOfWeek(Calendar.MONDAY)
                        .setPreselectedDate(time.get(Calendar.YEAR), time.get(Calendar.MONTH), time.get(Calendar.DAY_OF_MONTH))
                        .setDoneText(getString(R.string.fragment_events_date_picker_next))
                        .setCancelText(getString(R.string.fragment_events_cancel));
                cdp.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "DATE_PICKER");
                ((AppCompatActivity) getActivity()).getSupportFragmentManager().executePendingTransactions();
                setUpTimeAndDatePicker();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpTimeAndDatePicker();
    }

    public static final class MyRadialTimePickerDialogFragment extends RadialTimePickerDialogFragment {
        public MyRadialTimePickerDialogFragment setDate(int y, int m, int d) {
            year = y;
            month = m;
            day = d;
            return this;
        }

        private int
                year,
                month,
                day;

        public int getYear() {
            return year;
        }

        public int getMonth() {
            return month;
        }

        public int getDay() {
            return day;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("year", year);
            outState.putInt("month", month);
            outState.putInt("day", day);
        }

        @Override
        public void onCreate(Bundle state) {
            super.onCreate(state);
            if (state != null) {
                year = state.getInt("year");
                month = state.getInt("month");
                day = state.getInt("day");
            }
        }
    }

    private void setUpTimeAndDatePicker() {
        if (getActivity() == null) return;

        final CalendarDatePickerDialogFragment cdp = (CalendarDatePickerDialogFragment) ((AppCompatActivity) getActivity()).getSupportFragmentManager().findFragmentByTag("DATE_PICKER");
        if (cdp != null) {
            cdp.setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    Calendar time = null;
                    if (events != null && !events.isEmpty()) {
                        time = (Calendar) events.get(events.size() - 1).getTime().clone();
                        time.add(Calendar.HOUR, 1);
                    } else
                        time = startTime != null ? startTime : endTime != null ? endTime : Tools.prepareDate(System.currentTimeMillis());
                    final MyRadialTimePickerDialogFragment rtpd = ((MyRadialTimePickerDialogFragment) new MyRadialTimePickerDialogFragment()
                            .setStartTime(time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE))
                            .setDoneText(getString(R.string.fragment_events_time_picker_next))
                            .setCancelText(getString(R.string.fragment_events_cancel)))
                            .setDate(year, monthOfYear, dayOfMonth);
                    rtpd.show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "TIME_PICKER");
                    rtpd.setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                            Log.d(TAG, "onTimeSet: " + rtpd.getYear() + " " + rtpd.getMonth() + " " + rtpd.getDay() + " " + hourOfDay + " " + minute);
                            Event event = new Event(username, Tools.prepareDate(rtpd.getYear(), rtpd.getMonth() + 1, rtpd.getDay(), hourOfDay, minute, 0), "", "");

                            events.add(event);
                            Collections.sort(events);
                            adapter.notifyDataSetChanged();
                            pager.setAdapter(adapter);
                            pager.setCurrentItem(events.indexOf(event), false);
                            getActivity().invalidateOptionsMenu();
                        }
                    });
                    ((AppCompatActivity) getActivity()).getSupportFragmentManager().executePendingTransactions();
                    setUpTimeAndDatePicker();
                }
            });
            return;
        }

        final MyRadialTimePickerDialogFragment rtpd = (MyRadialTimePickerDialogFragment) ((AppCompatActivity) getActivity()).getSupportFragmentManager().findFragmentByTag("TIME_PICKER");
        if (rtpd != null) {
            rtpd.setOnTimeSetListener(new RadialTimePickerDialogFragment.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
                    Log.d(TAG, "onTimeSet: " + rtpd.getYear() + " " + rtpd.getMonth() + " " + rtpd.getDay() + " " + hourOfDay + " " + minute);
                    Event event = new Event(username, Tools.prepareDate(rtpd.getYear(), rtpd.getMonth() + 1, rtpd.getDay(), hourOfDay, minute, 0), "", "");

                    events.add(event);
                    Collections.sort(events);
                    adapter.notifyDataSetChanged();
                    pager.setAdapter(adapter);
                    pager.setCurrentItem(events.indexOf(event), false);
                    getActivity().invalidateOptionsMenu();
                }
            });
        }

    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.d(TAG, "onSaveInstanceState: ");
        Tools.saveSerializedObjectLogged(events, state, "events", TAG);
        Tools.saveSerializedObjectLogged(startTime, state, SET_TIME_START, TAG);
        Tools.saveSerializedObjectLogged(endTime, state, SET_TIME_END, TAG);
        state.putString(SET_USERNAME, username);
        if (pager != null)
            state.putInt("pager.currentItem", pager.getCurrentItem());
        super.onSaveInstanceState(state);
    }


    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        v = null;
//        statusBarLayout = null;
//        toolbar = null;
//        pager = null;
//        adapter = null;
        super.onDestroyView();
    }

    @Override
    public void onComponentEvent(@NonNull Object component, @Nullable Object contractObject, @Nullable Object stateComponent) {
        if (component instanceof FragmentEventsDetail) {
            if(service != null) {
                FragmentEventsDetail f = (FragmentEventsDetail) component;
                f.setUsername(username);
                if (contractObject instanceof Event) {
                    Event event = (Event) contractObject;
                    events.set(pager.getCurrentItem(), event);
                } else if (events != null) {
                    int i = (int) contractObject;
                    if (i < events.size())
                        f.setEvent(events.get(i));
                }
            } else synchronized (queueItems){
                queueItems.add(new QueueItem(component, contractObject, stateComponent));
            }
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private int count;

        private FragmentManager m;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            m = fm;
            count = events == null ? 0 : events.size();
        }

        @Override
        public synchronized void notifyDataSetChanged() {
            count = events == null ? 0 : events.size();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return count;
        }


        @Override
        public synchronized Fragment getItem(int position) {
            return FragmentEventsDetail.prepareFragmentEventsDetail(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return events.get(position).getTitle();
        }
    }

    public static FragmentEvents prepareFragmentEvents(Calendar timeStart, Calendar timeEnd, String username) {
        Bundle args = new Bundle();
        Tools.saveSerializedObjectLogged(timeStart, args, SET_TIME_START, TAG);
        Tools.saveSerializedObjectLogged(timeEnd, args, SET_TIME_END, TAG);
        args.putSerializable(SET_USERNAME, username);
        FragmentEvents f = new FragmentEvents();
        f.setArguments(args);
        return f;
    }

    private Calendar resetTimeToEnd(Calendar c) {
        c = resetTime(c);
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.add(Calendar.SECOND, -1);
        return c;
    }

    private Calendar resetTime(Calendar c) {
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        return c;
    }

    private int getConvertedDay(Calendar c) {
        int day = c.get(Calendar.DAY_OF_WEEK);
        return day == 1 ? 7 : day - 1;
    }
}
