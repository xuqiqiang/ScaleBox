package com.xuqiqiang.uikit.utils;

import android.util.Log;

import com.xuqiqiang.uikit.BuildConfig;

import java.util.Arrays;

@SuppressWarnings("unused")
public class Logger {
    public static final String TAG = "UIKit";
    public static boolean enabled = BuildConfig.DEBUG;

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

    public static void e(String tag, String msg) {
        if (!enabled) return;
        Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (!enabled) return;
        Log.e(tag, msg, e);
    }

    public static void d(String tag, String msg) {
        if (!enabled) return;
        Log.d(tag, msg);
    }

    public static void d(String tag, String msg, Throwable e) {
        if (!enabled) return;
        Log.d(tag, msg, e);
    }

    public static void d(String tag, String msg, Object[] arr) {
        if (!enabled) return;
        Log.d(tag, msg + ":" + Arrays.toString(arr));
    }
}
