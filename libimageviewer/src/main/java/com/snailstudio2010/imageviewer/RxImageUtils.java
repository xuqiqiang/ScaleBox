package com.snailstudio2010.imageviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.xuqiqiang.uikit.utils.BitmapUtils;
import com.xuqiqiang.uikit.utils.MimeUtils;
import com.xuqiqiang.uikit.utils.StringUtils;

/**
 * Created by xuqiqiang on 2019/09/04.
 */
public final class RxImageUtils {
    private static final String TAG = RxImageUtils.class.getSimpleName();
    private static final int MAX_BITMAP_SIZE = 1536;

    private RxImageUtils() {
    }

    public static void loadBitmap(Context context, Object url, LoadBitmapListener listener) {
        loadBitmap(context, url, 0, listener);
    }

    @SuppressLint("CheckResult")
    public static void loadBitmap(Context context, Object url, int limitSize, final LoadBitmapListener listener) {
//        long start = System.currentTimeMillis();
        if (context == null) return;
        if (limitSize > 0 && url instanceof String && !StringUtils.startsWithIgnoreCase((String) url, "http")
                && MimeUtils.isImage((String) url)) {
            Point size = BitmapUtils.getBitmapSize((String) url);
            if (size.x > limitSize || size.y > limitSize) {
                float widthRatio = (float) size.x / (float) limitSize;
                float heightRatio = (float) size.y / (float) limitSize;
                float ratio = Math.max(widthRatio, heightRatio);
                size.x /= ratio;
                size.y /= ratio;
                loadBitmap(context, url, size.x, size.y, listener);
                return;
            }
//            loadLocalBitmap(context, (String) url, limitSize, limitSize, listener);
//            return;
        }
        boolean isLocalFile = !(url instanceof String && StringUtils.startsWithIgnoreCase((String) url, "http"));
        try {
            final RequestBuilder<Bitmap> builder = Glide.with(context)
                    .asBitmap()
                    .load(url);
            if (isLocalFile) {
                builder.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE));
            }
            builder.into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                    Log.d(TAG, "resource:" + resource.isRecycled());
                    listener.onLoad(!resource.isRecycled() ? resource : null);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    Log.d(TAG, "onLoadError");
                    if (listener != null) listener.onLoad(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onLoad(null);
        }
    }

    @SuppressLint("CheckResult")
    public static void loadBitmap(Context context, Object url, int width, int height, final LoadBitmapListener listener) {
        long start = System.currentTimeMillis();
        if (context == null) return;
        boolean isLocalFile = !(url instanceof String && StringUtils.startsWithIgnoreCase((String) url, "http"));
        try {
            final RequestBuilder<Bitmap> builder = Glide.with(context)
                    .asBitmap();
            if (isLocalFile) {
                builder.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE));
            }

            builder.listener(new RequestListener<Bitmap>() {

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    Log.d(TAG, "resource:" + resource.isRecycled() + "," + resource.getWidth() + "," + resource.getHeight());
                    if (listener != null) {
                        listener.onLoad(!resource.isRecycled() ? resource : null);
                    }
                    return false;
                }

                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    Log.d(TAG, "onLoadError");
                    if (listener != null) listener.onLoad(null);
                    return false;
                }

            })
                    .load(url)
                    .preload(width, height);
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) listener.onLoad(null);
        }
    }

    public interface LoadBitmapListener {
        void onLoad(Bitmap bitmap);
    }
}
