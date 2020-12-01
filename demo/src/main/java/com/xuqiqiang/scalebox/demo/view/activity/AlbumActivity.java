package com.xuqiqiang.scalebox.demo.view.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.xuqiqiang.scalebox.ScaleBox;
import com.xuqiqiang.scalebox.ScaleBoxAdapter;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.controller.AlbumController;
import com.xuqiqiang.scalebox.demo.controller.PhotoController;
import com.xuqiqiang.scalebox.demo.model.entity.Album;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoWrapper;
import com.xuqiqiang.scalebox.demo.utils.FrescoImageLoader;
import com.xuqiqiang.scalebox.demo.view.adapter.GalleryAdapter;
import com.xuqiqiang.scalebox.demo.view.component.FastScrollerHelper;
import com.xuqiqiang.scalebox.demo.view.component.PhotoViewer;
import com.xuqiqiang.scalebox.utils.Logger;
import com.xuqiqiang.uikit.activity.BaseThemeActivity;
import com.xuqiqiang.uikit.utils.PermissionUtils;
import com.xuqiqiang.uikit.utils.ScreenUtils;
import com.xuqiqiang.uikit.utils.TimeUtils;
import com.xuqiqiang.uikit.view.LoadingView;

import java.util.ArrayList;
import java.util.List;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public class AlbumActivity extends BaseThemeActivity {

    private final PhotoController mPhotoController = new PhotoController();
    private final AlbumController mAlbumController = new AlbumController();
    private final List<PhotoEntity> mPhotoList = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private LoadingView mLoadingView;
    private TextView tvDate;
    private final AlbumController.OnDirectorySelectListener mDirectorySelectListener =
            new AlbumController.OnDirectorySelectListener() {
                @Override
                public void onSelect(final Album album) {
                    mDrawerLayout.close();
                    mLoadingView.showLoading("正在加载中～");
                    tvDate.setText("");
                    mToolbar.setTitle(album.getDisplayName());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPhotoController.resetLoad(album);
                        }
                    }, 200);
                }

                @Override
                public void onReset(final Album album) {
                    mDrawerLayout.close();
                    mToolbar.setTitle(album.getDisplayName());
                    tvDate.setText("");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mPhotoController.load(album);
                        }
                    }, 200);
                }
            };
    private ScaleBox mScaleBox;
    private GalleryAdapter mGalleryAdapter;
    private RecyclerView rvAlbums;
    private VerticalRecyclerViewFastScroller mFastScroller;
    private FastScrollerHelper mFastScrollerHelper;
    private PhotoViewer mPhotoViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenUtils.initialize(this);
        FrescoImageLoader.initialize(this);
        Logger.enabled = true;
        setContentView(R.layout.activity_album);

        rvAlbums = findViewById(R.id.rv_albums);
        rvAlbums.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle(Album.ALBUM_NAME_ALL_RES_ID);

        mDrawerLayout = findViewById(R.id.drawerlayout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerToggle.syncState();
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @SuppressWarnings("SuspiciousNameCombination")
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                View content = mDrawerLayout.getChildAt(0);
                float scale = 1 - slideOffset;//1~0
                float leftScale = (float) (1 - 0.3 * scale);
                float rightScale = (float) (0.7f + 0.3 * scale);//0.7~1
                drawerView.setScaleX(leftScale);//1~0.7
                drawerView.setScaleY(leftScale);//1~0.7

                content.setScaleX(rightScale);
                content.setScaleY(rightScale);
                content.setTranslationX(drawerView.getMeasuredWidth() * slideOffset);//0~width
                Logger.d("slideOffset=" + slideOffset + ",leftScale=" + leftScale + ",rightScale=" + rightScale);
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
//                Fresco.getImagePipeline().pause();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }
        });

        mPhotoViewer = findViewById(R.id.photo_viewer);

        mLoadingView = findViewById(R.id.loading_view);
        tvDate = findViewById(R.id.tv_date);
        mScaleBox = findViewById(R.id.scale_box);
        mGalleryAdapter = new GalleryAdapter(this, mPhotoList);
        mGalleryAdapter.setPhotoViewer(mPhotoViewer);
        mScaleBox.setAdapter(mGalleryAdapter);
        mGalleryAdapter.setShowPopupDate(true);

        mScaleBox.setOnScaleEventListener(new ScaleBox.OnScaleEventListener() {
            @Override
            public void onBeforeEvent() {
                Fresco.getImagePipeline().pause();
            }

            @Override
            public void onAfterEvent() {
                Fresco.getImagePipeline().resume();
            }
        });

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
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    mFastScrollerHelper.hideFastScroller();
//                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    mFastScrollerHelper.showFastScroller();
//                }
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
                mFastScrollerHelper.bindRecyclerView(mGalleryAdapter.getRecyclerView(level));
            }
        });
        mLoadingView.showLoading("正在加载中～");

        RecyclerView recyclerView = mGalleryAdapter.getRecyclerView(1);
        mFastScroller = findViewById(R.id.fast_scroller);
        mFastScrollerHelper = new FastScrollerHelper(mFastScroller);
        mFastScrollerHelper.bindRecyclerView(recyclerView);
    }

    private void updateDate(int level) {
        final GridLayoutManager layoutManager = (GridLayoutManager) mGalleryAdapter.getRecyclerView(level).getLayoutManager();
        if (layoutManager == null) return;
        int firstPosition = layoutManager.findFirstVisibleItemPosition();
        PhotoWrapper wrapper = mGalleryAdapter.getPhotoList(level).get(firstPosition);
        if (wrapper.isPhoto()) {
            tvDate.setText(TimeUtils.formatTime(wrapper.getPhotoEntity().getCreateTime(), "yyyy年MM月dd日"));
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
            mPhotoController.onCreate(this, mGalleryAdapter, mLoadingView, tvDate);
            mPhotoController.loadAllPhoto(this);
            mAlbumController.onCreate(this, rvAlbums, mDirectorySelectListener);
            mAlbumController.loadAlbums();
        }
        if (mPhotoViewer.isImageShowing())
            mPhotoViewer.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mPhotoViewer != null && mPhotoViewer.onBackPressed()) return;
        if (mDrawerLayout.isOpen()) {
            mDrawerLayout.close();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mAlbumController.onDestroy();
        mPhotoController.onDestroy();
        super.onDestroy();
    }
}
