package com.xuqiqiang.scalebox.utils;

import android.util.Log;

import java.util.Arrays;

@SuppressWarnings("unused")
public class Logger {
    public static final String TAG = "ScaleBox";
    public static boolean enabled = false;

    public static void e(String msg) {
        if (!enabled) return;
        Log.e(TAG, msg);
    }

    public static void e(String msg, Throwable e) {
        if (!enabled) return;
        Log.e(TAG, msg, e);
    }

    public static void d(String msg) {
        if (!enabled) return;
        Log.d(TAG, msg);
    }

    public static void d(String msg, Throwable e) {
        if (!enabled) return;
        Log.d(TAG, msg, e);
    }

    public static void d(String msg, Object[] arr) {
        if (!enabled) return;
        Log.d(TAG, msg + ":" + Arrays.toString(arr));
    }
}
