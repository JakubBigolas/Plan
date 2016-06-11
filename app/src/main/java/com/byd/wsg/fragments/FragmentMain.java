package com.byd.wsg.fragments;

import android.app.Fragment;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.plany.R;
import com.byd.wsg.services.Service;
import com.byd.wsg.services.TaskListener;
import com.byd.wsg.services.TaskStatus;

/**
 * Created by Jakub on 2016-05-29.
 */
public class FragmentMain extends Fragment implements OnComponentListener, ServiceConnection {

    private View
            v,
            panelLayout;

    private Button
            logoutButton;

    private TextView
            nameTextView,
            groupTextView;

    private View
            menuControlLayout,
            planControlLayout,
            eventsControlLayout;

    private OnComponentListener callbacks;

    private static final String TAG = "LOG-FragmentMain";
    private static final String FRAGMENT_MAIN_FRAGMENT = "fragment_main_fragment";

    private Service.Binder service;

    private MenuItem menuItem = MenuItem.MAIN_MENU;

    private enum MenuItem {
        MAIN_MENU, PLAN, EVENTS;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected: ");
        this.service = (Service.Binder) service;
        refreshMenuPanel();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected: ");
        this.service = null;
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
                getAppCompatActivity().getApplicationContext().unbindService(this);
            }
        } catch (Throwable t) {
            Log.d(TAG, "onStop: " + t.getLocalizedMessage());
        }
        service = null;
        super.onStop();
    }

    public AppCompatActivity getAppCompatActivity() {
        return (AppCompatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_main, container, false);
        panelLayout = v.findViewById(R.id.panelLayout);

        logoutButton = (Button) v.findViewById(R.id.logoutButton);
        nameTextView = (TextView) v.findViewById(R.id.nameTextView);
        groupTextView = (TextView) v.findViewById(R.id.groupTextView);
        menuControlLayout = v.findViewById(R.id.menuControlLayout);
        planControlLayout = v.findViewById(R.id.planControlLayout);
        eventsControlLayout = v.findViewById(R.id.eventsControlLayout);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt("menuItem", menuItem == null ? MenuItem.MAIN_MENU.ordinal() : menuItem.ordinal());
    }

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        if (state != null)
            menuItem = MenuItem.values()[state.getInt("menuItem", MenuItem.MAIN_MENU.ordinal())];

        if (getAppCompatActivity() instanceof OnComponentListener)
            callbacks = (OnComponentListener) getAppCompatActivity();

        if (getChildFragmentManager().findFragmentByTag(FRAGMENT_MAIN_FRAGMENT) == null)
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_main_container, new FragmentNews(), FRAGMENT_MAIN_FRAGMENT).commit();

        logoutButton.setOnClickListener(logoutListener);
        menuControlLayout.setOnClickListener(menuListener);
        planControlLayout.setOnClickListener(planListener);
        eventsControlLayout.setOnClickListener(eventsListener);

    }


    private void refreshMenuPanel() {
        if (v != null) {
            if (service != null) {
                if (service.getUser() != null)
                    nameTextView.setText(service.getUser() != null
                            ? service.getUser().getFirstName() + " " + service.getUser().getLastName()
                            : "");
                if (service.getStudent() != null && service.getStudent().getStudentGroup() != null && service.getStudent().getStudentGroup().getName() != null)
                    groupTextView.setText(service.getStudent() != null && service.getStudent().getStudentGroup() != null && service.getStudent().getStudentGroup().getName() != null
                            ? service.getStudent().getStudentGroup().getName()
                            : "");
            }
        }
        refreshMenuButtons();
    }

    public void refreshMenuButtons() {
        if (v != null) {
            View vMenu = menuControlLayout.findViewById(R.id.view1);
            View vPlan = planControlLayout.findViewById(R.id.view2);
            View vEvents = eventsControlLayout.findViewById(R.id.view3);

            vMenu.setBackgroundColor(menuItem == MenuItem.MAIN_MENU ? 0xFFFF4400 : 0xFF888888);
            vPlan.setBackgroundColor(menuItem == MenuItem.PLAN ? 0xFFFF4400 : 0xFF888888);
            vEvents.setBackgroundColor(menuItem == MenuItem.EVENTS ? 0xFFFF4400 : 0xFF888888);
        }
    }

    View.OnClickListener logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (callbacks != null && service != null) {
                service.logout();
                callbacks.onComponentEvent(FragmentMain.this, null, null);
            }
        }
    };

    View.OnClickListener menuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuItem = MenuItem.MAIN_MENU;
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_main_container, new FragmentNews(), FRAGMENT_MAIN_FRAGMENT).commit();
            refreshMenuButtons();
        }
    };

    View.OnClickListener planListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuItem = MenuItem.PLAN;
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_main_container, new FragmentTimeTable(), FRAGMENT_MAIN_FRAGMENT).commit();
            refreshMenuButtons();
        }
    };

    View.OnClickListener eventsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuItem = MenuItem.EVENTS;
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_main_container, new FragmentEvents(), FRAGMENT_MAIN_FRAGMENT).commit();
            refreshMenuButtons();
        }
    };

    @Override
    public void onComponentEvent(@NonNull Object component, @Nullable Object contractObject, @Nullable Object stateComponent) {
        Fragment f = getChildFragmentManager().findFragmentByTag(FRAGMENT_MAIN_FRAGMENT);
        if (f != null && f instanceof OnComponentListener)
            ((OnComponentListener) f).onComponentEvent(component, contractObject, stateComponent);
    }
}
