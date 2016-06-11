package com.byd.wsg.fragments;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.byd.wsg.com.wsg.byd.plan.Plan;
import com.byd.wsg.com.wsg.byd.plan.TimeTable;
import com.byd.wsg.example.ExampleData;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;
import com.byd.wsg.services.Service;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.transform.sax.TransformerHandler;

/**
 * Created by Jakub on 2016-04-23.
 */
public class FragmentTimeTable extends Fragment implements OnComponentListener, ServiceConnection {
    private View v;
    private View statusBarLayout;
    private Toolbar toolbar;
    private ViewPager pager;
    private MyPagerAdapter adapter;
    private Plan plan;
    private String username;// TODO
    OnComponentListener callbacks;

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
        TimeTable timeTable = null;
        if (((Service.Binder) service).getNews() != null)
            timeTable = this.service.getNews().getTimeTable();
        username = ((Service.Binder) service).getUser().getLogin();
        if (timeTable != null) {
            try {
                plan = ExampleData.prepareExampleTimeTable();
                plan.setTimeTable(timeTable);
                if (adapter == null) adapter = new MyPagerAdapter(getChildFragmentManager());
                if (plan.isShort() != getActivity().getSharedPreferences(FragmentTimeTable.class.getName(), 0).getBoolean("plan.short", plan.isShort()))
                    switchTimeTable();
                else if (v != null) {
                    pager.setAdapter(adapter);
                }
                if (pager != null) {
                    int day = timeTable.getCurrentDayTableIndex();
                    if (day >= 0) pager.setCurrentItem(day);
                    pager.getAdapter().notifyDataSetChanged();
                }
                synchronized (queueItems) {
                    for (QueueItem q : queueItems)  // BO SIE CHAMSKI ANDROID UPARL ZE NIE ODSIWEZY I CH....J
                        onComponentEvent(q.o1, q.o2, q.o3);
                    queueItems.clear();
                }
            } catch (ParseException e) {
                Log.d(TAG, "onServiceConnected: " + e.getLocalizedMessage());
            }
        }
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart:");
        if (service == null) {
            Intent intent = new Intent(getActivity(), Service.class);
            getAppCompatActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
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

    public static final String TAG = "LOG-FragmentTimeTable";

    public AppCompatActivity getAppComatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnComponentListener)
            callbacks = (OnComponentListener) context;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchFullyShortTimeTable:
                if (plan == null)
                    break;
//                pager.setAdapter(adapter);
//                adapter.notifyDataSetChanged();
                switchTimeTable();
                if (plan != null)
                    item.setIcon(plan.isShort()
                            ? R.drawable.ic_date_range_white_short_24dp
                            : R.drawable.ic_date_range_white_24dp);
                break;
//            case R.id.event:
//                Log.d(TAG, "onOptionsItemSelected: " + (pager != null) + " : " + (plan != null) + " : " + (callbacks != null));
//                if (pager != null && plan != null && callbacks != null && !plan.getTimeTable().isEmpty()) {
//
//                    Calendar timeStart = plan.getTimeTable().getDayTable(pager.getCurrentItem()).getDate();
//                    Calendar timeEnd = (Calendar) timeStart.clone();
//                    timeEnd.add(Calendar.DAY_OF_YEAR, 1);
//                    callbacks.onComponentEvent(this, FragmentEvents.prepareFragmentEvents(timeStart, timeEnd, username), null);
//                }
//                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_time_table, menu);
        MenuItem switchFullyShortTimeTable = menu.findItem(R.id.switchFullyShortTimeTable);
        if (plan != null)
            switchFullyShortTimeTable.setIcon(plan.isShort()
                    ? R.drawable.ic_date_range_white_short_24dp
                    : R.drawable.ic_date_range_white_24dp);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        if (getActivity() instanceof OnComponentListener) // W LOLIPOPIE NIE WYWOLUJE MI onAttach
            callbacks = (OnComponentListener) getActivity();
        if (state == null)
            state = getArguments();
        Log.d(TAG, "onCreate:" + (state != null));

        if (service == null) {
            Intent intent = new Intent(getActivity(), Service.class);
            getAppCompatActivity().getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        }

        super.onCreate(state);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView:");
        v = inflater.inflate(R.layout.fragment_time_table, null, false);
        statusBarLayout = v.findViewById(R.id.statusBarLayout);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        pager = (ViewPager) v.findViewById(R.id.viewPager);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        if (state == null)
            state = getArguments();
        Log.d(TAG, "onActivityCreated:" + (state != null));
        getAppComatActivity().setSupportActionBar(toolbar);
        getAppComatActivity().getSupportActionBar().setTitle(R.string.fragment_plan_toolbar_title);
//        adapter = new MyPagerAdapter(getChildFragmentManager());
//        adapter.notifyDataSetChanged();
//        pager.setAdapter(adapter);
        if (state != null) pager.setCurrentItem(state.getInt("pager.currentItem", 0));
        statusBarLayout.bringToFront();
        setHasOptionsMenu(true);
    }

    public void switchTimeTable() {
        Plan p = plan.switchTimeTable();
        if (p != null) {
            plan = p;
            if (pager != null && adapter != null) {
                int page = pager.getCurrentItem();
                pager.setAdapter(adapter);
                pager.setCurrentItem(page, false);
            }
            getAppComatActivity().getSharedPreferences(FragmentTimeTable.class.getName(), 0).edit().putBoolean("plan.short", plan.isShort()).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.d(TAG, "onSaveInstanceState: ");
        if (pager != null)
            state.putInt("pager.currentItem", pager.getCurrentItem());
        super.onSaveInstanceState(state);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        v = null;
        statusBarLayout = null;
        toolbar = null;
        pager = null;
        adapter = null;
        super.onDestroyView();
    }

    @Override
    public void onComponentEvent(@NonNull Object component, @Nullable Object contractObject, @Nullable Object stateComponent) {
        if (component instanceof FragmentTimeTableList) {
            FragmentTimeTableList f = (FragmentTimeTableList) component;
            if (service != null) {
                f.setUsername(service.getUser().getLogin());
                int i = (int) contractObject;
                if (plan != null && plan.getTimeTable() != null && i < plan.getTimeTable().size())
                    f.setDayTable(plan.getTimeTable().getDayTable(i));
            }
           else  synchronized (queueItems) {
                queueItems.add(new QueueItem(component, contractObject, stateComponent));
            }
        } else if (component instanceof FragmentEditEvent) {
            int page = pager.getCurrentItem();
            pager.setAdapter(adapter);
            pager.setCurrentItem(page);
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private int count;

        private FragmentManager m;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
            m = fm;
            count = plan == null || plan.getTimeTable() == null ? 0 : plan.getTimeTable().size();
        }

        @Override
        public synchronized void notifyDataSetChanged() {
            count = plan == null || plan.getTimeTable() == null ? 0 : plan.getTimeTable().size();
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public int getItemPosition(Object object) {
            Log.d(TAG, "getItemPosition: " + object.getClass().getName());
            return POSITION_NONE;
        }

        @Override
        public synchronized Fragment getItem(int position) {
            Log.d(TAG, "FragmentPagerAdapter.getItem: ");
            return FragmentTimeTableList.prepareFragment(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return plan == null || plan.getTimeTable() == null ? "" : plan.getTimeTable().getDayTable(position).getDayLocaleString() + " " +
                    plan.getTimeTable().getDayTable(position).getDateLocaleString();
        }
    }

}
