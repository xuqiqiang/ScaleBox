package com.xuqiqiang.scalebox.demo.controller;

import android.app.Activity;
import android.app.LoaderManager;
import android.database.Cursor;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public abstract class BaseLoaderController implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int PHOTO_LOADER_ID = 1;
    public static final int ALBUM_LOADER_ID = 2;

    protected Activity context;

    protected LoaderManager loaderManager;

    protected void onCreate(Activity activity) {
        context = activity;
        loaderManager = activity.getLoaderManager();
    }

    public void onDestroy() {
        loaderManager.destroyLoader(getLoaderId());
    }

    protected abstract int getLoaderId();
}
