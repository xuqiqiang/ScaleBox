package com.snailstudio2010.imageviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import indi.liyi.viewer.ImageLoader;
import indi.liyi.viewer.R;

/**
 * Created by xuqiqiang on 2019/07/17.
 */
public class PhotoLoader extends ImageLoader {
    private static final String TAG = "PhotoLoader";
    private static Bitmap mPlaceholder;
    //    protected ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private boolean hasLoadBitmap;
    private Bitmap mCachedBitmap;

    public static Bitmap getPlaceholder(Context context) {
        if (mPlaceholder == null) {
            mPlaceholder = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_default);
        }
        return mPlaceholder;
    }

    public void setCachedBitmap(Bitmap cachedBitmap) {
        this.mCachedBitmap = cachedBitmap;
    }

    @Override
    public void displayImage(final Object src, final int state, final ImageView imageView, final LoadCallback callback) {
        hasLoadBitmap = false;
        Log.d(TAG, "displayImage state:" + state + "," + src);
        if (state == 0) {
            if (callback != null) {
                if (mCachedBitmap != null) {
                    callback.onLoadStarted(mCachedBitmap);
                    mCachedBitmap = null;
                    final long start = System.currentTimeMillis();
                    RxImageUtils.loadBitmap(imageView.getContext(), src, 200, 200, new RxImageUtils.LoadBitmapListener() {
                        @Override
                        public void onLoad(Bitmap bitmap) {
                            if (!hasLoadBitmap) {
                                Log.d("test", "_test_i_ getDrawingCache 1:" + (bitmap.getWidth()) + " " + bitmap.getHeight() + " " + (System.currentTimeMillis() - start));
                                callback.onLoadStarted(bitmap);
                            }
                        }
                    });
                } else {
                    callback.onLoadStarted(getPlaceholder(imageView.getContext()));
                    RxImageUtils.loadBitmap(imageView.getContext(), src, 200, 200, new RxImageUtils.LoadBitmapListener() {
                        @Override
                        public void onLoad(Bitmap bitmap) {
                            if (!hasLoadBitmap) {
                                callback.onLoadStarted(bitmap);
                            }
                        }
                    });
                }
            }
        } else if (state == 1) {
            RxImageUtils.loadBitmap(imageView.getContext(), src, 200, 200, new RxImageUtils.LoadBitmapListener() {
                @Override
                public void onLoad(Bitmap bitmap) {
                    if (callback == null || !callback.onLoadSucceed(bitmap)) {
//                            Logger.d("src:" + src);
//                            RxImageUtils.loadImage(imageView.getContext(), (String) src, imageView);
                        imageView.setImageBitmap(bitmap);
                    }

                }
            });
            return;
        }

        RxImageUtils.loadBitmap(imageView.getContext(), src, new RxImageUtils.LoadBitmapListener() {
            @Override
            public void onLoad(Bitmap bitmap) {
                hasLoadBitmap = true;
                if (bitmap != null) {
                    if (callback == null || !callback.onLoadSucceed(bitmap)) {
                        imageView.setImageBitmap(bitmap);
                    }
                } else {
                    if (callback != null) {
                        callback.onLoadFailed(getPlaceholder(imageView.getContext()));
                    }
                }
            }
        });
        return;
    }
}
