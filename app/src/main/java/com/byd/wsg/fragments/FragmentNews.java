package com.byd.wsg.fragments;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.byd.wsg.com.wsg.byd.plan.News;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;
import com.byd.wsg.services.Service;
import com.byd.wsg.services.TaskListener;
import com.byd.wsg.services.TaskStatus;

import org.w3c.dom.Text;

/**
 * Created by Jakub on 2016-06-11.
 */
public class FragmentNews extends Fragment implements ServiceConnection {

    private View v, statusBarLayout;

    private static final String TAG = "LOG-FragmentNews";
    private Service.Binder service;
    private View thisDayLayout, thisWeekLayout, thisMohthLayout;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;



    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: ");
        this.service = (Service.Binder) service;
        this.service.setNewsListener(newsListener);
        newsListener.sendEvent(this.service.getNewsStatus(), this.service.getNewsError(), this.service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        this.service = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_news, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit:
                Intent intent = new Intent(getActivity(), Service.class);
                getActivity().stopService(intent);
                getActivity().onBackPressed();
                break;
            case R.id.refresh:
                if(service != null) service.refreshNews(newsListener);
                refreshNewsLayout();
                break;
        }
        return super.onOptionsItemSelected(item);
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
                service.removeNewsListener(newsListener);
                getAppCompatActivity().getApplicationContext().unbindService(this);
            }
        } catch (Throwable t) {
            Log.d(TAG, "onStop: " + t.getLocalizedMessage());
        }
        service = null;
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNewsLayout();
    }

    TaskListener newsListener = new TaskListener() {
        @Override
        public void onEvent(TaskStatus taskStatus, Throwable error, Service.Binder service) {
            refreshNewsLayout();
        }
    };

    private void refreshNewsLayout() {
        if (v != null && service != null) {
            TaskStatus taskStatus = service.getNewsStatus();
            News news = service.getNews();
            if(taskStatus != null) switch (taskStatus) {
                case STARTED:
                    progressBar.setVisibility(View.VISIBLE);
                    thisDayLayout.setVisibility(View.GONE);
                    thisWeekLayout.setVisibility(View.GONE);
                    thisMohthLayout.setVisibility(View.GONE);
                    break;
                case INTERNET_ACCES_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_internet_access_error);
                    hideContent();
                    break;
                case IO_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_io_error);
                    hideContent();
                    break;
                case UNEXPECTED_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_unexpected_error);
                    hideContent();
                    break;
                case AUTHENICATION_FAILED:
                    Tools.toastRs(getActivity(), R.string.service_authentication_failed);
                    hideContent();
                    break;
                case TIMEOUT:
                    Tools.toastRs(getActivity(), R.string.service_timeout);
                    hideContent();
                    break;
                case UNAUTHORIZED_ERROR:
                    Tools.toastRs(getActivity(), R.string.service_unauthorized_error);
                    hideContent();
                    break;
                case CANCELLED:
                    hideContent();
                    break;
                case DONE:
                    progressBar.setVisibility(View.GONE);
                    if (news != null) {
                        setPeriodView(thisDayLayout, news.getThisDay());
                        setPeriodView(thisWeekLayout, news.getThisWeek());
                        setPeriodView(thisMohthLayout, news.getThisMonth());
                    }
                    break;
            } else {
                progressBar.setVisibility(View.VISIBLE);
                thisDayLayout.setVisibility(View.GONE);
                thisWeekLayout.setVisibility(View.GONE);
                thisMohthLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setPeriodView(View periodView, News.PeriodOfTime period) {
        if (periodView != null && period != null) {
            periodView.setVisibility(View.VISIBLE);
            TextView lessonsCountTextView = (TextView) periodView.findViewById(R.id.lessonsCountTextVIew);
            TextView eventsCountTextView = (TextView) periodView.findViewById(R.id.eventsCountTextView);
            if (lessonsCountTextView != null) lessonsCountTextView.setText(period.getCurrentCountOfLessons() + "/" + period.getCountOfLessons());
            if (eventsCountTextView != null) eventsCountTextView.setText(period.getCurrentCountOfEnvents() + "/" + period.getCountOfEvents());
        }
    }

    private void hideContent() {
        thisDayLayout.setVisibility(View.GONE);
        thisWeekLayout.setVisibility(View.GONE);
        thisMohthLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_news, container, false);

        thisDayLayout = v.findViewById(R.id.thisDayLayout);
        thisWeekLayout = v.findViewById(R.id.thisWeekLayout);
        thisMohthLayout = v.findViewById(R.id.thisMonthLayout);
        toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        statusBarLayout = v.findViewById(R.id.statusBarLayout);
        coordinatorLayout = (CoordinatorLayout) v.findViewById(R.id.coordinatorLayout);
        appBarLayout = (AppBarLayout) v.findViewById(R.id.appBarLayout);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        ((TextView) thisDayLayout.findViewById(R.id.periodTextView)).setText(getText(R.string.fragment_news_this_day));
        ((TextView) thisWeekLayout.findViewById(R.id.periodTextView)).setText(getText(R.string.fragment_news_this_week));
        ((TextView) thisMohthLayout.findViewById(R.id.periodTextView)).setText(getText(R.string.fragment_news_this_month));

        hideContent();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle status) {
        super.onActivityCreated(status);

        statusBarLayout.bringToFront();
        getAppCompatActivity().setSupportActionBar(toolbar);
        setHasOptionsMenu(true);
        getAppCompatActivity().getSupportActionBar().setTitle(R.string.fragment_news_toolbar_title);


    }
}
