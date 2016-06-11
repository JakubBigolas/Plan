package com.byd.wsg.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.byd.wsg.com.wsg.byd.plan.DayTable;
import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.com.wsg.byd.plan.Interval;
import com.byd.wsg.com.wsg.byd.plan.Lesson;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jakub on 2016-04-23.
 */
public class FragmentTimeTableList extends Fragment {

    private View v;
    private LinearLayout list;
    private DayTable dayTable;
    private MyArrayAdapter adapter;

    private View
            timeToNowView,
            currentTimeView;
    private int accentColor, timeToNowColor, timeFromNowColor, breakColor;
    private ScrollView scrollView;
    private int position;
    OnComponentListener callbacks;


//    private FragmentEditEvent fragmentEditEvent;

    private String username = "";

    public static final String
            TAG = "LOG-FragmentTimeTableL.",
            SET_USERNAME = "FragmentTimeTableList.SET_USERNAME",
            SET_POSITION = "FragmentTimeTableList.SET_POSITION";

    BroadcastReceiver changeTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "changeTimeReceiver.onReceive: " + intent.getAction());
            refreshView();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnComponentListener)
            callbacks = (OnComponentListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        getActivity().registerReceiver(changeTimeReceiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(changeTimeReceiver);
        refreshView();
        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle state) {
        super.onCreate(state);
        Log.d(TAG, "onCreate: " + (dayTable != null));
        if (getActivity() instanceof OnComponentListener)
            callbacks = (OnComponentListener) getActivity();
        if (state == null)
            state = getArguments();
        if (state != null) {
            username = state.getString(SET_USERNAME, username);
            position = state.getInt(SET_POSITION);
        }
    }

    public void setDayTable(DayTable dayTable) {
        this.dayTable = dayTable;
        Log.d(TAG, "setDayTable: 1 " + accentColor);
        if (adapter != null) {
            Log.d(TAG, "setDayTable: 2");
            adapter.notifyDataSetChanged();
            refreshList();
        }
    }

    public DayTable getDayTable() {
        return dayTable;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        v = null;
        list = null;
        adapter = null;
        super.onDestroyView();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView:");
        v = inflater.inflate(R.layout.fragment_time_table_list, null, false);
        list = (LinearLayout) v.findViewById(R.id.linearLayout);
        timeToNowView = v.findViewById(R.id.timeToNowView);
        currentTimeView = v.findViewById(R.id.currentTimeView);
        scrollView = (ScrollView) v.findViewById(R.id.scrollView);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle state) {
        super.onActivityCreated(state);

        if (callbacks != null)
            callbacks.onComponentEvent(this, position, null);
        if (dayTable == null)
            dayTable = new DayTable();

        Resources resources = getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Resources.Theme theme = getActivity().getTheme();
            accentColor = resources.getColor(R.color.colorAccent, theme);
            timeToNowColor = resources.getColor(R.color.timeToColor, theme);
            timeFromNowColor = resources.getColor(R.color.timeFromColor, theme);
        } else {
            accentColor = resources.getColor(R.color.colorAccent);
            timeToNowColor = resources.getColor(R.color.timeToColor);
            timeFromNowColor = resources.getColor(R.color.timeFromColor);
        }
        adapter = new MyArrayAdapter();
        refreshList();
        Tools.restoreParcelableObject(scrollView, state, "list");

//        FragmentManager f = getChildFragmentManager();
//        fragmentEditEvent = (FragmentEditEvent) f.findFragmentByTag("dialogAddNote");
//        if (fragmentEditEvent == null)
//            fragmentEditEvent = new FragmentEditEvent();

        if (callbacks != null)
            callbacks.onComponentEvent(this, position, null);
        Log.d(TAG, "onActivityCreated: " + super.toString());
    }


    private void refreshList() {
        list.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++)
            list.addView(adapter.getView(i, null, null));
        list.invalidate();
        refreshView();
    }

    private void refreshView() {
        if (v != null) {
            currentTimeView.setVisibility(View.INVISIBLE);

            list.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Log.d(TAG, "refreshView: today");
                    try {
                        if (_isToday()) {
                            timeToNowView.setVisibility(View.VISIBLE);
                            currentTimeView.setVisibility(View.INVISIBLE);
                            final long currMin = (System.currentTimeMillis() - dayTable.getDate().getTimeInMillis()) / MILLIS_PER_MINUTE;
                            int currTimeHeight = 0;
                            boolean stoppedLoop = false;
                            for (int i = 0; i < dayTable.size(); i++) {
                                final Interval interval = dayTable.getInterval(i);
                                View row = list.getChildAt(i);
                                if (currMin > interval.getEnd()) {
                                    currTimeHeight += row.getHeight();
                                } else if (currMin >= interval.getStart()) {
                                    int h = (int) (row.getHeight() * _getPercentOfCurrentInterval(interval, currMin));
                                    if (h > row.getHeight()) h = row.getHeight();
                                    h -= currentTimeView.getHeight();
                                    if (h < 0) h = 0;
                                    currTimeHeight += h;
                                    currentTimeView.setVisibility(View.VISIBLE);
                                    break;
                                } else {
                                    stoppedLoop = true;
                                    break;
                                }
                            }
                            if (currentTimeView.getVisibility() == View.VISIBLE || stoppedLoop) {
                                ViewGroup.LayoutParams layoutParams = timeToNowView.getLayoutParams();
                                layoutParams.height = currTimeHeight;
                                timeToNowView.setLayoutParams(layoutParams);
                            } else if (!stoppedLoop && dayTable.size() > 0) {
                                ViewGroup.LayoutParams layoutParams = timeToNowView.getLayoutParams();
                                layoutParams.height = !_isNextDay() ? Math.max(v.getHeight(), list.getHeight()) : 0;
                                timeToNowView.setLayoutParams(layoutParams);
                                currentTimeView.setVisibility(View.GONE);
                            }
                        } else {
                            ViewGroup.LayoutParams layoutParams = timeToNowView.getLayoutParams();
                            layoutParams.height = !_isNextDay() ? Math.max(v.getHeight(), list.getHeight()) : 0;
                            timeToNowView.setLayoutParams(layoutParams);
                            currentTimeView.setVisibility(View.GONE);
                        }
                        list.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } catch (Throwable t) { // NP GDY ZA SZYBKO PRZELACZYMY WIDOKI ALBO ZAKOCZNYMY AKTYWNOSC PRZED WYKONANIEM TEGO KODU
                        Log.d(TAG, "onGlobalLayout: " + t.getLocalizedMessage());
                        try {
                            list.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } catch (Throwable tt) {
                        }
                    }
                }
            });
        }
    }

    public static FragmentTimeTableList prepareFragment(int position) {
        FragmentTimeTableList result = new FragmentTimeTableList();
        Bundle args = new Bundle();
        args.putInt(SET_POSITION, position);
        result.setArguments(args);
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.d(TAG, "onSaveInstanceState: ");
        Tools.saveParcelcableObject(list, state, "list");
        state.putInt(SET_POSITION, position);
        super.onSaveInstanceState(state);
    }

    private boolean _hasLastTime() {
        return dayTable != null && !dayTable.isEmpty();
    }

    private long _getLastTime() {
        return _hasLastTime()
                ? dayTable.getDate().getTimeInMillis() + dayTable.getLesson(dayTable.size() - 1).getEnd()
                : dayTable.getDate().getTimeInMillis();
    }

    private boolean _isFullDayTimeTable() {
        return _hasLastTime() && dayTable.getInterval(dayTable.size() - 1).getEnd() >= MINUTES_PER_DAY;
    }

    private static final long MINUTES_PER_DAY = 1440;

    private static final long MILLIS_PER_DAY = 86400000L;

    private static final long MILLIS_PER_MINUTE = 60000L;

    private boolean _isToday() {
        long result = dayTable.getDate().getTimeInMillis() - System.currentTimeMillis();
        return result <= 0 && result > -MILLIS_PER_DAY;
    }

    private boolean _isNextDay() {
        return dayTable.getDate().getTimeInMillis() - System.currentTimeMillis() > 0;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private class MyArrayAdapter extends ArrayAdapter {

        LayoutInflater inflater;

        public MyArrayAdapter() {
            super(getActivity(), 0);
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public int getCount() {
            return dayTable != null ? dayTable.size() : 0;
        }

        @Override
        public View getView(final int position, View v, ViewGroup parent) {
            switch (dayTable.getInterval(position).getType()) {
                case BREAK:
                    v = inflater.inflate(R.layout.fragment_time_table_break, null, false);
                    break;
                case LESSON:
                    v = inflater.inflate(R.layout.fragment_time_table_row, null, false);
                    break;
                case FREE:
                    v = inflater.inflate(R.layout.fragment_time_table_row_free_time, null, false);
                    break;
            }

            final TextView
                    roomTextView = (TextView) v.findViewById(R.id.roomTextView),
                    withTextView = (TextView) v.findViewById(R.id.withTextView),
                    subjectTextView = (TextView) v.findViewById(R.id.subjectTextView),
                    startTextView = (TextView) v.findViewById(R.id.startTextView),
                    endTextView = (TextView) v.findViewById(R.id.endTextView),
                    typeTextView = (TextView) v.findViewById(R.id.typeTextView);

            View eventIndicator = v.findViewById(R.id.eventIndicator);
            Interval interval = dayTable.getInterval(position);
            ArrayList<Event> events = null;
            final Calendar startTime = dayTable.getDate();
            Calendar endTime = dayTable.getDate();
            try {
                Event.Helper helper = new Event.Helper(new SQLiteHelper(getActivity()), Event.Helper.TABLE_NAME);

                startTime.add(Calendar.MINUTE, interval.getStart());
                endTime.add(Calendar.MINUTE, interval.getEnd());
                events = helper.read(
                        username,
                        startTime, true,
                        endTime, false,
                        null, null);
            } catch (Throwable t) {
                Log.d(TAG, "getView: " + t.getLocalizedMessage());
                throw new RuntimeException(t);
            }

            switch (interval.getType()) {
                case LESSON:
                    Lesson lesson = (Lesson) interval;
                    roomTextView.setText(lesson.getRoom());
                    withTextView.setText(lesson.getSubject().getWith());
                    subjectTextView.setText(lesson.getSubject().getTitle());
                    typeTextView.setText(lesson.getSubject().getType());

                    GradientDrawable background = (GradientDrawable) roomTextView.getBackground();
                    background.setColor(getColorForFirstChar(lesson.getRoom()));
                    background = (GradientDrawable) typeTextView.getBackground();
                    background.setColor(accentColor);
                case FREE:
                    v.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            FragmentEditEvent fragmentEditEvent = new FragmentEditEvent();
                            fragmentEditEvent.setEvent(new Event(username, startTime, "", ""));
                            fragmentEditEvent.show(getChildFragmentManager(), "dialogAddNote");
                            return true;
                        }
                    });
                case BREAK:
                    startTextView.setText(Tools.convertMinutesToString(dayTable.getInterval(position).getStart()));
                    endTextView.setText(Tools.convertMinutesToString(dayTable.getInterval(position).getEnd()));
                    eventIndicator.setVisibility(events != null && !events.isEmpty() ? View.VISIBLE : View.GONE);
                    break;
            }
            return v;
        }
    }

    public double _getPercentOfCurrentInterval(Interval i, long minute) {
        if (i.getStart() == minute)
            return 0;
        return (double) (minute - i.getStart()) / (double) (i.getEnd() - i.getStart());
    }

    public int getColorForFirstChar(String txt) {
        if (txt == null || txt.length() == 0)
            return accentColor;
        txt = txt.substring(0, 1);
        txt = txt.toLowerCase();
        char[] character = new char[1];
        txt.getChars(0, 1, character, 0);

        final int FIRST_CHAR = 97;
        int posColor = (int) character[0] - FIRST_CHAR;
        int[] colors = getResources().getIntArray(R.array.roomsColors);
        return posColor > colors.length - 1 || posColor < 0 ? accentColor : colors[posColor];
    }

}
