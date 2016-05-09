package com.byd.wsg.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import com.byd.wsg.example.ExampleData;
import com.byd.wsg.fragments.FragmentTimeTableList;
import com.byd.wsg.model.Tools;
import com.byd.wsg.plany.R;

import java.text.ParseException;
import java.util.List;

/**
 * Created by Jakub on 2016-05-06.
 */
public class DialogAddNote extends DialogFragment implements LoaderManager.LoaderCallbacks{


    private View v;
    private View statusBarLayout;
    private Toolbar toolbar;
    private ViewPager pager;
    private MyPagerAdapter adapter;
    private Plan plan;

    public static final String TAG = "LOG-FragmentTimeTable";

    public AppCompatActivity getAppComatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switchFullyShortTimeTable:
                if (plan == null)
                    break;
                switchTimeTable();
                if (plan != null)
                    item.setIcon(plan.isShort()
                            ? R.drawable.ic_date_range_white_short_24dp
                            : R.drawable.ic_date_range_white_24dp);
                break;
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
        if (state == null)
            state = getArguments();
        Log.d(TAG, "onCreate:" + (state != null));
        if (state != null) {
            try {
                plan = (Plan) Tools.loadDeserializedObject(state, "plan");
                if (plan == null) getLoaderManager().initLoader(0, null, this);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
        super.onCreate(state);
        setCancelable(true);
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
        adapter = new MyPagerAdapter(getChildFragmentManager());
        adapter.notifyDataSetChanged();
        pager.setAdapter(adapter);
        if (state != null) pager.setCurrentItem(state.getInt("pager.currentItem", 0));
        statusBarLayout.bringToFront();
        setHasOptionsMenu(true);
    }


    public static void log(Object msg) {
        Log.d(TAG, msg.toString());
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");

        return new AsyncTaskLoader(getActivity()) {
            @Override
            public Object loadInBackground() {
                Log.d(TAG, "loadInBackground: LOADER");
                try {
                    return ExampleData.prepareExampleTimeTable();// TODO DANE TYMCZASOWE DO TESTÓW
                } catch (ParseException e) {
                    Log.d(TAG, "loadInBackground: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onStartLoading() {
                Log.d(TAG, "onStartLoading: ");
                super.onStartLoading();
                forceLoad();

            }
        };
    }

    public void switchTimeTable() {
        Plan p = plan.switchTimeTable();
        if (p != null) {
            plan = p;
            if (adapter != null)
                adapter.refreshPages();
            getAppComatActivity().getSharedPreferences(DialogAddNote.class.getName(), 0).edit().putBoolean("plan.short", plan.isShort()).commit();
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.d(TAG, "onLoadFinished: ");
        plan = ((Plan) data);
        if (adapter != null) adapter.notifyDataSetChanged();
        if (plan.isShort() != getActivity().getSharedPreferences(DialogAddNote.class.getName(), 0).getBoolean("plan.short", plan.isShort()))
            switchTimeTable();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.d(TAG, "onLoaderReset: ");
        plan = null;
        if (adapter != null) adapter.notifyDataSetChanged();
    }


    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.d(TAG, "onSaveInstanceState: ");
        Tools.saveSerializedObjectLogged(plan, state, "plan", TAG);
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

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private int count;

        private android.support.v4.app.FragmentManager m;

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            m = fm;
        }

        @Override
        public void notifyDataSetChanged() {
            count = plan == null || plan.getTimeTable() == null ? 0 : plan.getTimeTable().size();
            super.notifyDataSetChanged();
        }

        public void refreshPages() {
            List<Fragment> pages = m.getFragments();
            if (pages == null) return;
            for (int i = 0; i < m.getFragments().size(); i++) {
                FragmentTimeTableList f = (FragmentTimeTableList) pages.get(i);
                if (f != null) {
                    Log.d(TAG, "refreshPages: " + i + " / " + f.toString());
                    f.setDayTable(plan.getTimeTable().getDayTable(i));

                }
            }
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            Log.d(TAG, "FragmentPagerAdapter.getItem: ");
            FragmentTimeTableList f = FragmentTimeTableList.prepareFragment(plan.getTimeTable().getDayTable(position));
            return FragmentTimeTableList.prepareFragment(plan.getTimeTable().getDayTable(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return plan == null || plan.getTimeTable() == null ? "" : plan.getTimeTable().getDayTable(position).getDayLocaleString() + " " +
                    plan.getTimeTable().getDayTable(position).getDateLocaleString();
        }
    }


}
