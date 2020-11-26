package com.xuqiqiang.scalebox.demo.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.scalebox.demo.model.entity.Album;
import com.xuqiqiang.scalebox.demo.model.loader.PhotoLoader;
import com.xuqiqiang.uikit.utils.TimeUtils;
import com.xuqiqiang.scalebox.demo.view.adapter.GalleryAdapter;
import com.xuqiqiang.uikit.view.LoadingView;
import com.xuqiqiang.scalebox.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public class PhotoController extends BaseLoaderController {

    private static final String ARGS_ALBUM = "ARGS_ALBUM";
    private GalleryAdapter mGalleryAdapter;
    private LoadingView mLoadingView;
    private TextView mTvDate;
    private Cursor mCursor;
    private int mRowIDColumn;

    public void onCreate(@NonNull Activity context, GalleryAdapter galleryAdapter,
                         LoadingView loadingView, TextView tvDate) {
        super.onCreate(context);
        mGalleryAdapter = galleryAdapter;
        mLoadingView = loadingView;
        mTvDate = tvDate;
    }

    @Override
    protected int getLoaderId() {
        return PHOTO_LOADER_ID;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Album album = args.getParcelable(ARGS_ALBUM);
        if (album == null) {
            return null;
        }
        return PhotoLoader.newInstance(context, album);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) oldCursor.close();
        new DownloadImageTask(mGalleryAdapter, mLoadingView, mTvDate).execute(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor oldCursor = swapCursor(null);
        if (oldCursor != null) oldCursor.close();
        mGalleryAdapter.setDate(null);
    }

    /**
     * @param album album
     */
    public void load(Album album) {
//        mLoadingView.showLoading("正在加载中～");
        Bundle args = new Bundle();
        args.putParcelable(ARGS_ALBUM, album);
        loaderManager.initLoader(getLoaderId(), args, this);
    }

    /**
     * @param context context
     */
    public void loadAllPhoto(Context context) {
        Album album = new Album(Album.ALBUM_ID_ALL, -1,
                context.getString(Album.ALBUM_NAME_ALL_RES_ID), 0);
        load(album);
    }

    /**
     * restartLoader will cancel, stop and destroy the loader (and close the
     * underlying data source like a cursor) and create a new loader(which would also create a new
     * cursor and re-run the query if the loader is a CursorLoader).
     *
     * @param target album
     */
    public void resetLoad(Album target) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_ALBUM, target);
        loaderManager.restartLoader(getLoaderId(), args, this);
    }

    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
        } else {
            mRowIDColumn = -1;
        }
        return oldCursor;
    }

    private static class DownloadImageTask extends AsyncTask<Cursor, Void, List<PhotoEntity>> {
        private final GalleryAdapter galleryAdapter;
        private final LoadingView loadingView;
        private final TextView tvDate;

        public DownloadImageTask(GalleryAdapter galleryAdapter, LoadingView loadingView, TextView tvDate) {
            this.galleryAdapter = galleryAdapter;
            this.loadingView = loadingView;
            this.tvDate = tvDate;
        }

        protected List<PhotoEntity> doInBackground(Cursor... cursors) {
            Cursor cursor = cursors[0];
            List<PhotoEntity> photoList = new ArrayList<>();
            while (!cursor.isClosed() && cursor.moveToNext()) {
                String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Logger.d("_test_ filePath:" + filePath);
                photoList.add(new PhotoEntity(filePath));
            }
            return photoList;
        }

        protected void onPostExecute(List<PhotoEntity> result) {
            if (result.isEmpty()) {
                loadingView.showEmpty("还没有照片");
            } else {
                loadingView.onComplete();
                tvDate.setText(TimeUtils.formatTime(result.get(0).getCreateTime(), "yyyy年MM月dd日"));
            }
            galleryAdapter.setDate(result);
        }
    }
}
