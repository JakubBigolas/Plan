package com.byd.wsg.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.fragments.FragmentLogin;
import com.byd.wsg.fragments.FragmentMain;
import com.byd.wsg.fragments.FragmentTimeTable;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.plany.R;
import com.byd.wsg.services.Service;

/**
 * Created by Jakub on 2016-04-23.
 */
public class ActivityMain extends AppCompatActivity implements OnComponentListener, ServiceConnection {

    private Service.Binder service;

    private static final String TAG_MAIN_FRAGMENT = "main_fragment";

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: ");
        this.service = (Service.Binder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        this.service = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:  ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, Service.class);
        startService(intent);

        FragmentManager m = getFragmentManager();
        if (m.findFragmentByTag(TAG_MAIN_FRAGMENT) == null)
            m.beginTransaction().replace(R.id.mainContainer, new FragmentLogin(), TAG_MAIN_FRAGMENT).commit();
//                m.beginTransaction().replace(R.id.mainContainer, new FragmentTimeTable(), TAG_MAIN_FRAGMENT).commit();
        new Event.Helper(new SQLiteHelper(this), Event.Helper.TABLE_NAME).setUpDatabase();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        Intent intent = new Intent(this, Service.class);
        Log.d(TAG, "onStart: " + getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE));
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        try {
            if (service != null)
                getApplicationContext().unbindService(this);
        } catch (Throwable t) {
            Log.d(TAG, "onStop: " + t.getLocalizedMessage());
        }
        service = null;
        super.onStop();
    }

    @Override
    public void onComponentEvent(@NonNull Object component, @Nullable Object contractObject, @Nullable Object statusComponent) {
        Log.d(TAG, "onComponentEvent: asdfsdaf " + (component instanceof FragmentLogin) );
        Log.d(TAG, "onComponentEvent: asdfsdaf " + (component.getClass().getCanonicalName()) );
        Log.d(TAG, "onComponentEvent: asdfsdaf " + (FragmentLogin.class.getCanonicalName()) );
        if (component instanceof FragmentTimeTable) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();

            transaction.setCustomAnimations(
                    R.animator.fragment_events_in,
                    R.animator.fragment_time_table_out,
                    R.animator.fragment_time_table_in,
                    R.animator.fragment_events_out);

            transaction.replace(R.id.mainContainer, (Fragment) contractObject, TAG_MAIN_FRAGMENT).addToBackStack(null);
            transaction.commit();

        } else if(component instanceof FragmentLogin) {
            getFragmentManager().beginTransaction().replace(R.id.mainContainer, new FragmentMain(), TAG_MAIN_FRAGMENT).commit();
        } else if(component instanceof FragmentMain){
            getFragmentManager().beginTransaction().replace(R.id.mainContainer, new FragmentLogin(), TAG_MAIN_FRAGMENT).commit();
        } else {
            Fragment f = getFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT);
            if (f instanceof OnComponentListener)
                ((OnComponentListener) f).onComponentEvent(component, contractObject, statusComponent);
        }
    }

    @Override// CHODZI O TO ŻE APPCOMPATACTIVITY IGNORUJE STOS FRAGMENTMANAGERA (POWINIEN PRACOWAC Z SUPPORTU)
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            if(getFragmentManager().findFragmentByTag(TAG_MAIN_FRAGMENT) instanceof FragmentLogin) {
                Intent intent = new Intent(this, Service.class);
                stopService(intent);
            }
            super.onBackPressed();
        }
    }


    public static String TAG = "LOG-ActivityMain";
}
