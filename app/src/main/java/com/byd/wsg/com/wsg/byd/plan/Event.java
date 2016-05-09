package com.byd.wsg.com.wsg.byd.plan;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.byd.wsg.model.Tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Jakub on 2016-05-01.
 */
public class Event implements Serializable {

    long id;
    String user = "";
    Calendar time;
    String title = "";
    String content = "";

    /**
     * @param user    null will be conwerted to empty string
     * @param time    null will be converted to current time
     * @param title   null will be conwerted to empty string
     * @param content null will be conwerted to empty string
     */
    public Event(String user, Calendar time, String title, String content) {
        this(-1, user, time, title, content);
    }

    /**
     * create object with default values, for strings : empty string, for dates : current time.
     */
    public Event() {
        time = Tools.prepareDate(System.currentTimeMillis());
    }

    public Event(Event event) {
        this(-1, event.user, event.getTime(), event.title, event.content);
    }

    private Event(int id, String user, Calendar time, String title, String content) {
        this.id = id;
        this.user = user == null ? "" : user;
        this.time = time == null ? Tools.prepareDate(System.currentTimeMillis()) : time;
        this.title = title == null ? "" : title;
        this.content = content == null ? "" : content;
    }

    // GETTERS -----------------------------------------------

    private long getId() {
        return id;
    }

    @NonNull
    public String getUser() {
        return user;
    }

    @NonNull
    public Calendar getTime() {
        return time;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    // SETTERS -----------------------------------------------

    private void setId(long id) {
        this.id = id;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @param content null will be conwerted to empty string.
     */
    public void setContent(String content) {
        this.content = content == null ? "" : content;
    }


    /**
     * @param title null will be conwerted to empty string.
     */
    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    /**
     * @param time null will be converted to current time.
     */
    public void setTime(Calendar time) {
        this.time = time == null ? Tools.prepareDate(System.currentTimeMillis()) : time;
    }

    public static class Helper {

        public enum Columns {
            ID("ID"), USER("USER"), TIME("TIME"), TITLE("TITLE"), CONTENT("CONTENT");

            Columns(String databaseName) {
                this.dbn = databaseName;
            }

            private final String dbn;

            public final String dbn() {
                return dbn;
            }

            public static final String[] allDbNames() {
                String[] result = new String[Columns.values().length];
                for (int i = 0; i < Columns.values().length; i++)
                    result[i] = Columns.values()[i].dbn();
                return result;
            }
        }

        public static final String TABLE_NAME = "EVENT";

        private SQLiteOpenHelper sql;

        private String tableName;

        public Helper(SQLiteOpenHelper sqLiteOpenHelper, String tableName) {
            sql = sqLiteOpenHelper;
            this.tableName = tableName;
        }

        public void setUpDatabase() throws SQLiteException {
            SQLiteDatabase db = sql.getWritableDatabase();

            //    db.execSQL("DROP TABLE " + tableName);

            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " ( " +
                            Columns.ID.dbn() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            Columns.USER.dbn() + " TEXT NOT NULL, " +
                            Columns.TIME.dbn() + " INTEGER NOT NULL, " +
                            Columns.TITLE.dbn() + " TEXT NOT NULL, " +
                            Columns.CONTENT.dbn() + " TEXT NOT NULL " +
                            " ) ");

            db.close();
        }

        /**
         * @param user    null will be conwerted to empty string
         * @param time    null will be converted to current time
         * @param title   null will be conwerted to empty string
         * @param content null will be conwerted to empty string
         *                return new instance added to database or null if could not inset data
         */
        public final Event create(String user, Calendar time, String title, String content) throws SQLiteException {
            return create(new Event(user, time, title, content));
        }

        /**
         * @param event null will be converted to default instance
         *              return new instance added to database or null if could not inset data
         */
        public final Event create(Event event) {
            SQLiteDatabase db = sql.getWritableDatabase();
            if (event == null) event = new Event();

            ContentValues values = new ContentValues();
            values.put(Columns.USER.dbn(), event.getUser());
            values.put(Columns.TIME.dbn(), event.getTime().getTimeInMillis());
            values.put(Columns.TITLE.dbn(), event.getTitle());
            values.put(Columns.CONTENT.dbn(), event.getContent());

            long id = db.insert(tableName, null, values);
            if (id < 0)
                return null;
            event = new Event(event);
            event.setId(id);
            db = sql.getReadableDatabase();
            db.close();
            return event;
        }

        /**
         * @param where
         * @param column
         * @return 1 if added to clause, otherwise 0
         */
        private int _concWhere(StringBuilder where, Columns column, Object value, String operator) throws SQLiteException {
            if (value != null) {
                where.append((where.length() > 0 ? " AND " : "") + column.dbn + (operator == null ? " = " : " " + operator + " ") + " ? ");
                return 1;
            }
            return 0;
        }

        /**
         * @param user        null will be ommited
         * @param timeFrom    null will be ommited
         * @param includeFrom if timeFrom is null then will be ommited
         * @param timeTo      null will be ommited
         * @param includeTo   if timeTo is null then will be ommited
         * @param title       null will be ommited
         * @param content     null will be ommited
         * @return
         */
        @NonNull
        public ArrayList<Event> read(String user, Calendar timeFrom, boolean includeFrom, Calendar timeTo, boolean includeTo, String title, String content) throws SQLiteException {
            ArrayList<Event> result = new ArrayList<>();
            SQLiteDatabase db = sql.getReadableDatabase();

            StringBuilder where = new StringBuilder();
            int args = _concWhere(where, Columns.USER, user, null);
            args += _concWhere(where, Columns.TITLE, title, null);
            args += _concWhere(where, Columns.CONTENT, content, null);
            args += _concWhere(where, Columns.TIME, timeFrom, includeFrom ? ">=" : ">");
            args += _concWhere(where, Columns.TIME, timeTo, includeTo ? "<=" : "<");

            String clause = args > 0 ? where.toString() : null;
            String[] vargs = null;
            if (args > 0) {
                vargs = new String[args];
                int i = 0;
                if (user != null) vargs[i++] = user;
                if (title != null) vargs[i++] = title;
                if (content != null) vargs[i++] = content;
                if (timeFrom != null) vargs[i++] = timeFrom.getTimeInMillis() + "";
                if (timeTo != null) vargs[i++] = timeTo.getTimeInMillis() + "";
            }


            Cursor c = db.query(
                    tableName,
                    Columns.allDbNames(),
                    clause,
                    vargs,
                    null, null, null);
            if (c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToNext();
                    Event n = new Event();
                    result.add(n);
                    Columns col = null;
                    for (int a = 0; a < Columns.values().length; a++)
                        switch (col = Columns.values()[a]) {
                            case CONTENT:
                                n.setContent(c.getString(col.ordinal()));
                                break;
                            case ID:
                                n.setId(c.getLong(col.ordinal()));
                                break;
                            case TIME:
                                n.setTime(Tools.prepareDate(c.getLong(col.ordinal())));
                                break;
                            case TITLE:
                                n.setTitle(c.getString(col.ordinal()));
                                break;
                            case USER:
                                n.setUser(c.getString(col.ordinal()));
                                break;
                        }
                }
                c.close();
            }
            db.close();
            return result;
        }

        /**
         * @param user    null will be ommited
         * @param time    null will be ommited
         * @param title   null will be ommited
         * @param content null will be ommited
         * @return
         */
        @NonNull
        public ArrayList<Event> read(String user, Calendar time, String title, String content) throws SQLiteException {
            ArrayList<Event> result = new ArrayList<>();
            SQLiteDatabase db = sql.getReadableDatabase();

            StringBuilder where = new StringBuilder();
            int args = _concWhere(where, Columns.USER, user, null);
            args += _concWhere(where, Columns.TIME, time, null);
            args += _concWhere(where, Columns.TITLE, title, null);
            args += _concWhere(where, Columns.CONTENT, content, null);
            String clause = args > 0 ? where.toString() : null;

            String[] vargs = null;
            if (args > 0) {
                vargs = new String[args];
                int i = 0;
                if (user != null) vargs[i++] = user;
                if (time != null) vargs[i++] = time.getTimeInMillis() + "";
                if (title != null) vargs[i++] = title;
                if (content != null) vargs[i++] = content;
            }

            Cursor c = db.query(
                    tableName,
                    Columns.allDbNames(),
                    clause,
                    vargs,
                    null, null, null);
            if (c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToNext();
                    Event n = new Event();
                    result.add(n);
                    Columns col = null;
                    for (int a = 0; a < Columns.values().length; a++)
                        switch (col = Columns.values()[a]) {
                            case CONTENT:
                                n.setContent(c.getString(col.ordinal()));
                                break;
                            case ID:
                                n.setId(c.getLong(col.ordinal()));
                                break;
                            case TIME:
                                n.setTime(Tools.prepareDate(c.getLong(col.ordinal())));
                                break;
                            case TITLE:
                                n.setTitle(c.getString(col.ordinal()));
                                break;
                            case USER:
                                n.setUser(c.getString(col.ordinal()));
                                break;
                        }
                }
                c.close();
            }
            db.close();
            return result;
        }


        @NonNull
        public ArrayList<Event> read(Event event) throws SQLiteException {
            if (event != null)
                return read(event.getUser(), event.getTime(), event.getTitle(), event.getContent());
            return readAll();
        }

        @NonNull
        public ArrayList<Event> readAll() throws SQLiteException {
            ArrayList<Event> result = new ArrayList<>();
            SQLiteDatabase db = sql.getReadableDatabase();
            Cursor c = db.query(
                    tableName,
                    Columns.allDbNames(),
                    null, null, null, null, null);
            if (c != null) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToNext();
                    Event n = new Event();
                    result.add(n);
                    Columns col = null;
                    for (int a = 0; a < Columns.values().length; a++)
                        switch (col = Columns.values()[a]) {
                            case CONTENT:
                                n.setContent(c.getString(col.ordinal()));
                                break;
                            case ID:
                                n.setId(c.getLong(col.ordinal()));
                                break;
                            case TIME:
                                n.setTime(Tools.prepareDate(c.getLong(col.ordinal())));
                                break;
                            case TITLE:
                                n.setTitle(c.getString(col.ordinal()));
                                break;
                            case USER:
                                n.setUser(c.getString(col.ordinal()));
                                break;
                        }
                    }
                c.close();
            }
            db.close();
            return result;
        }

        public final boolean update(Event event) throws SQLiteException {
            SQLiteDatabase db = sql.getWritableDatabase();
            if (event == null || event.getId() < 0)
                return false;

            ContentValues values = new ContentValues();
            values.put(Columns.USER.dbn(), event.getUser());
            values.put(Columns.TIME.dbn(), event.getTime().getTimeInMillis());
            values.put(Columns.TITLE.dbn(), event.getTitle());
            values.put(Columns.CONTENT.dbn(), event.getContent());

            boolean result = db.update(tableName, values, Columns.ID.dbn() + " = ?", new String[]{Long.toString(event.getId())}) > 0;
            db.close();
            return result;
        }

        public final boolean delete(Event event) throws SQLiteException {
            SQLiteDatabase db = sql.getWritableDatabase();
            if (event == null || event.getId() < 0)
                return false;
            boolean result = db.delete(tableName, Columns.ID.dbn() + " = ?", new String[]{Long.toString(event.getId())}) > 0;
            db.close();
            return result;
        }

    }
}
