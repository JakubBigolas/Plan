package com.byd.wsg.model;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Calendar;

/**
 * Created by Jakub on 2016-04-23.
 */
public class Tools {
    public static void saveSerializedObject(@Nullable
                                            Serializable o, Bundle state, String key) throws IOException {
        byte[] buff = serializeObject(o);
        if (buff != null) {
            state.putByteArray(key, buff);
        }
    }

    public static void toast(Context context, Object msg) {
        Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).show();
    }

    public static void toastRs(Context context, int stringRes){
        Toast.makeText(context, context.getString(stringRes), Toast.LENGTH_SHORT).show();
    }

    /**
     * @param o
     * @param state
     * @param key
     * @param TAG   if null then throw RuntimeException when it occurs
     */
    public static void saveSerializedObjectLogged(@Nullable Serializable o, Bundle state, String key, String TAG) throws RuntimeException {
        try {
            byte[] buff = serializeObject(o);
            if (buff != null) {
                state.putByteArray(key, buff);
            }
        } catch (Throwable t) {
            if (TAG != null) Log.d(TAG, "saveSerializedObjectLogged: " + t.getLocalizedMessage());
            else throw new RuntimeException(t);
        }
    }

    @Nullable
    public static byte[] serializeObject(@Nullable
                                         Serializable o) throws IOException {
        if (o == null)
            return null;
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(o);
        return byteOut.toByteArray();
    }

    @Nullable
    public static Object loadDeserializedObjectLogged(Bundle state, String key, Object defaultValue, String TAG) {
        Object result = null;
        try {
            result = deserializeObject(state.getByteArray(key));
        } catch (Throwable t) {
            Log.d(TAG, "loadDeserializedObjectLogged: " + t.getLocalizedMessage());
        }
        return result != null ? result : defaultValue;
    }

    @Nullable
    public static Object loadDeserializedObjectLogged(Bundle state, String key, Class classForDefaultInstance, String TAG) {
        try {
            Object result = null;
            try {
                result = deserializeObject(state.getByteArray(key));
            } catch (Throwable t) {
                Log.d(TAG, "loadDeserializedObjectLogged: " + t.getLocalizedMessage());
            }
            return result != null ? result : classForDefaultInstance != null ? classForDefaultInstance.newInstance() : null;
        } catch (Throwable t) {
            Log.d(TAG, "loadDeserializedObjectLogged: " + t.getLocalizedMessage());
            return null;
        }
    }

    @Nullable
    public static Object loadDeserializedObjectLogged(Bundle state, String key, String TAG) {
        try {
            return deserializeObject(state.getByteArray(key));
        } catch (Throwable t) {
            Log.d(TAG, "loadDeserializedObjectLogged: " + t.getLocalizedMessage());
            return null;
        }
    }

    @Nullable
    public static Object loadDeserializedObject(Bundle state, String key, Object defaultValue)
            throws StreamCorruptedException, ClassNotFoundException,
            IOException, IllegalAccessException, InstantiationException {
        Object result = deserializeObject(state.getByteArray(key));
        return result != null ? result : defaultValue;
    }

    @Nullable
    public static Object loadDeserializedObject(Bundle state, String key, Class classForDefaultInstance)
            throws StreamCorruptedException, ClassNotFoundException,
            IOException, IllegalAccessException, InstantiationException {
        Object result = deserializeObject(state.getByteArray(key));
        return result != null ? result : classForDefaultInstance != null ? classForDefaultInstance.newInstance() : null;
    }

    @Nullable
    public static Object loadDeserializedObject(Bundle state, String key)
            throws StreamCorruptedException, ClassNotFoundException,
            IOException {
        return deserializeObject(state.getByteArray(key));
    }

    @Nullable
    public static Object deserializeObject(@Nullable
                                           byte[] data) throws StreamCorruptedException, IOException,
            ClassNotFoundException {
        if (data == null)
            return null;
        ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return in.readObject();
    }

    public static final void log(Object o) {
        Log.d("LOG", o.toString());
    }

    /**
     * @return true if saved anything
     */
    public static boolean saveDialogConditionally(String name, Dialog dialog, Bundle state, Bundle lastState) {
        if (state == null)
            return false;
        if (dialog != null) {
            state.putBundle(name, dialog.onSaveInstanceState());
            return true;
        }
        if (lastState == null)
            return false;
        state.putBundle(name, lastState.getBundle(name));
        return true;
    }

    public static boolean restoreParcelableObject(Object o, Bundle state, String name) {
        if (o == null || name == null || state == null)
            return false;
        try {
            Parcelable p = state.getParcelable(name);
            if (p == null)
                return false;
            o.getClass().getMethod("onRestoreInstanceState", Parcelable.class).invoke(o, p);
            return true;
        } catch (Throwable t) {
        }
        return false;
    }

    public static boolean saveParcelcableObject(Object o, Bundle state, String name) {
        if (state == null)
            return false;
        if (o != null)
            try {
                Parcelable p = (Parcelable) o.getClass().getMethod("onSaveInstanceState", null).invoke(o, null);
                state.putParcelable(name, p);
            } catch (Throwable e) {
                return false;
            }
        return false;
    }

    public static boolean saveParcelcableObject(Object o, Bundle state, String name, Bundle lastState) {
        if (state == null)
            return false;
        if (o != null)
            try {
                Parcelable p = (Parcelable) o.getClass().getMethod("onSaveInstanceState", null).invoke(o, null);
                state.putParcelable(name, p);
            } catch (Throwable e) {
                return false;
            }
        else if (lastState != null) {
            state.putParcelable(name, lastState.getParcelable(name));
        }
        return false;
    }

    public static Calendar prepareDate(int y, int mo, int d, int h, int mi, int s) {
        Calendar result = Calendar.getInstance();
        result.clear();
        result.set(y, mo - 1, d, h, mi, s);
        return result;
    }

    public static Calendar prepareDate(int y, int mo, int d) {
        Calendar result = Calendar.getInstance();
        result.clear();
        result.set(y, mo - 1, d);
        return result;
    }

    public static Calendar prepareDate(long millis) {
        Calendar result = Calendar.getInstance();
        result.clear();
        result.setTimeInMillis(millis);
        return result;
    }

    public static String convertMinutesToString(long min) {
        return String.format("%02d:%02d", min / 60, min % 60);
    }
}
