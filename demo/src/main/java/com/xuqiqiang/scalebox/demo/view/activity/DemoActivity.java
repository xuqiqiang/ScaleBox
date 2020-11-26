package com.xuqiqiang.scalebox.demo.view.activity;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.xuqiqiang.scalebox.ScaleBox;
import com.xuqiqiang.scalebox.ScaleBoxAdapter;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoWrapper;
import com.xuqiqiang.scalebox.demo.model.entity.Album;
import com.xuqiqiang.scalebox.demo.model.loader.PhotoLoader;
import com.xuqiqiang.scalebox.demo.utils.FrescoImageLoader;
import com.xuqiqiang.uikit.utils.ScreenUtils;
import com.xuqiqiang.uikit.utils.TimeUtils;
import com.xuqiqiang.scalebox.demo.view.adapter.GalleryAdapter;
import com.xuqiqiang.scalebox.utils.Logger;
import com.xuqiqiang.uikit.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2020/07/12.
 */
public class DemoActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARGS_ALBUM = "ARGS_ALBUM";
    public static final int PHOTO_LOADER_ID = 1;
    private Cursor mCursor;
    private int mRowIDColumn;
    private LoaderManager loaderManager;
    private List<PhotoEntity> mPhotoList = new ArrayList<>();
    private TextView tvDate;
    private ScaleBox mScaleBox;
    private GalleryAdapter mGalleryAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.initialize(this);
        FrescoImageLoader.initialize(this);
        Logger.enabled = true;
        setContentView(R.layout.activity_demo);
        tvDate = findViewById(R.id.tv_date);
        loaderManager = getLoaderManager();
        mScaleBox = findViewById(R.id.scale_box);
        mGalleryAdapter = new GalleryAdapter(this, mPhotoList);
        mScaleBox.setAdapter(mGalleryAdapter);
        mGalleryAdapter.setShowPopupDate(true);

        mGalleryAdapter.addOnScrollListener(new ScaleBoxAdapter.OnScrollListener() {
            @Override
            public void onScrollStateChanged(int level, @NonNull RecyclerView recyclerView, int newState) {
                if (level >= 4) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        Fresco.getImagePipeline().resume();
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        Fresco.getImagePipeline().pause();
                    }
                }
//                if (newState == RecyclerView.SCROLL_STATE_IDLE && level == mGalleryAdapter.getLevel()) {
//                    updateDate(level);
//                }
            }

            @Override
            public void onScrolled(int level, @NonNull RecyclerView recyclerView, int dx, int dy) {
                if (level == mGalleryAdapter.getLevel()) {
                    updateDate(level);
                }
            }
        });
        mGalleryAdapter.setOnScaleListener(new ScaleBoxAdapter.OnScaleListener() {
            @Override
            public void onScaleChanged(int level) {
                updateDate(level);
            }
        });
    }

    private void updateDate(int level) {
        final GridLayoutManager layoutManager = (GridLayoutManager) mGalleryAdapter.getRecyclerView(level).getLayoutManager();
        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        PhotoWrapper wrapper = mGalleryAdapter.getPhotoList(level).get(firstPosition);
        if (wrapper.isPhoto()) {
            tvDate.setText(TimeUtils.formatTime(wrapper.getPhotoEntity().getCreateTime(), "yyyy年MM月dd日"));
            //wrapper.getPhotoEntity().getDate());
        } else {
            tvDate.setText(wrapper.getDate());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.checkPermission(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PermissionUtils.isPermissionGranted(this) && mPhotoList.isEmpty()) {
            loadAllPhoto();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Logger.d("_story_ onCreateLoader:" + id);
        Album album = args.getParcelable(ARGS_ALBUM);
        if (album == null) {
            return null;
        }
        return PhotoLoader.newInstance(this, album);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Cursor oldCursor = swapCursor(cursor);
        if (oldCursor != null) oldCursor.close();
        new DownloadImageTask().execute(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor oldCursor = swapCursor(null);
        if (oldCursor != null) oldCursor.close();
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

    public void load(Album album) {
        Bundle args = new Bundle();
        args.putParcelable(ARGS_ALBUM, album);
        loaderManager.initLoader(PHOTO_LOADER_ID, args, this);
    }

    public void loadAllPhoto() {
        Album album = new Album(Album.ALBUM_ID_ALL, -1,
                getString(Album.ALBUM_NAME_ALL_RES_ID), 0);
        load(album);
    }

    private class DownloadImageTask extends AsyncTask<Cursor, Void, List<PhotoEntity>> {

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
            mPhotoList.clear();
            mPhotoList.addAll(result);
            mGalleryAdapter.notifyDataSetChanged(true);
        }
    }

}
