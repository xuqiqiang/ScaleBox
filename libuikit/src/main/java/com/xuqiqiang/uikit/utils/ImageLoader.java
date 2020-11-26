/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Libutils
 */
package com.xuqiqiang.uikit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageLoader {

    private int maxSize = (int) (Runtime.getRuntime().freeMemory() / 2);
    private LruCache<String, Bitmap> memCache = new LruCache<String, Bitmap>(maxSize) {

        @Override
        protected int sizeOf(@NonNull String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }

    };
    private File cacheDir;

    public ImageLoader(Context context) {
        cacheDir = context.getCacheDir();
    }

    public Bitmap getBitmap(String imgUrl) {

        Bitmap bitmap = memCache.get(imgUrl);
        if (bitmap != null) {
            return bitmap;
        }

        bitmap = getCacheFile(imgUrl);
        if (bitmap != null) {
            memCache.put(imgUrl, bitmap);
            return bitmap;
        }

        bitmap = getBitmapFromNet(imgUrl);
        if (bitmap != null) {
            memCache.put(imgUrl, bitmap);
            saveBitmapToCacheFile(bitmap, imgUrl);
        }
        return bitmap;
    }

    public void saveBitmapToCacheFile(Bitmap bitmap, String imgUrl) {
        File file = new File(cacheDir, Md5.getStringMD5(imgUrl));
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getCacheFile(String imgUrl) {
        File file = new File(cacheDir, Md5.getStringMD5(imgUrl));
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            memCache.put(imgUrl, bitmap);
            return bitmap;
        } else {
            return null;
        }
    }

    private Bitmap getBitmapFromNet(String imgUrl) {
        Bitmap bitmap = null;
        try {

            URL url = new URL(imgUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(6000);
            con.setRequestMethod("GET");
            int code = con.getResponseCode();
//            logD("responseCode:" + code);
            if (code == 200) {
                InputStream inputStream = con.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap = ImageUtils.centerCrop(bitmap);
            }

//            URL url = new URL(ivUrl);
//            String responseCode = url.openConnection().getHeaderField(0);
//            logD("responseCode:" + responseCode);
//            bitmap = BitmapFactory.decodeStream(url.openStream());
//            if (!bitmap.isRecycled()) {
//                bitmap = ImageUtils.centerCrop(bitmap);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}