package com.byd.wsg.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.byd.wsg.com.wsg.byd.plan.Event;
import com.byd.wsg.fragments.FragmentTimeTable;
import com.byd.wsg.model.OnComponentListener;
import com.byd.wsg.model.SQLiteHelper;
import com.byd.wsg.plany.R;

import java.util.List;

/**
 * Created by Jakub on 2016-04-23.
 */
public class ActivityMain extends AppCompatActivity implements OnComponentListener {

    private static final String TAG_MAIN_FRAGMENT = "main_fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:  ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager m = getSupportFragmentManager();
        if (m.findFragmentByTag(TAG_MAIN_FRAGMENT) == null)
            m.beginTransaction().replace(R.id.mainContainer, new FragmentTimeTable(), TAG_MAIN_FRAGMENT).commit();

        new Event.Helper(new SQLiteHelper(this), Event.Helper.TABLE_NAME).setUpDatabase();


    }

    @Override
    public void onComponentEvent(@NonNull Object component, @Nullable Object contractObject,@Nullable Object statusComponent) {
        FragmentManager fm = getSupportFragmentManager();
        List<Fragment> fragments = fm.getFragments();
        if (fragments != null)
            for (Fragment f : fragments)
                if (f instanceof OnComponentListener)
                    ((OnComponentListener) f).onComponentEvent(component, contractObject, statusComponent);
    }

    public static String TAG = "LOG-ActivityMain";
}
