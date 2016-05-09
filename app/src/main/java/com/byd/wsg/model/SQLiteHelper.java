package com.byd.wsg.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jakub on 2016-05-02.
 */
public class SQLiteHelper extends SQLiteOpenHelper {


    public SQLiteHelper(Context context) {
        super(context, "com_byd_wsg_model_SQLiteHelper.db", null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
