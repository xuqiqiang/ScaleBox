package com.xuqiqiang.uikit.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by xuqiqiang on 2019/09/05.
 */
public class SingleTaskHandler extends Handler {
    private Runnable mTask;
    private long mRunTime;

    public SingleTaskHandler() {
    }

    public SingleTaskHandler(Looper looper) {
        super(looper);
    }

    public SingleTaskHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    public boolean postDelayedTask(final Runnable r, long delayMillis) {
        if (mTask != null) {
            removeCallbacks(mTask);
        }
        mTask = new Runnable() {
            @Override
            public void run() {
                mTask = null;
                if (r != null) r.run();
            }
        };
        return postDelayed(mTask, delayMillis);
    }

    public boolean postDelayedTaskLowPriority(final Runnable r, long delayMillis) {
        if (mTask != null) return false;
        mTask = new Runnable() {
            @Override
            public void run() {
                mTask = null;
                if (r != null) r.run();
            }
        };
        return postDelayed(mTask, delayMillis);
    }

    public final Runnable removeTask() {
        Runnable task = mTask;
        if (mTask != null) {
            removeCallbacks(mTask);
            mTask = null;
        }
        return task;
    }

    public Runnable postTask(final Runnable r, final long delayMillis) {
        return new Runnable() {
            @Override
            public void run() {
                post(r, delayMillis);
            }
        };
    }

    public void post(Runnable r, long delayMillis) {
        long now = System.currentTimeMillis();
        if (now - mRunTime > delayMillis) {
            mRunTime = now;
            if (r != null) r.run();
        }
    }
}