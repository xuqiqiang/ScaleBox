package com.xuqiqiang.scalebox.demo.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public interface ImageLoader {
  void bindImage(ImageView imageView, Uri uri, int width, int height);

  void bindImage(ImageView imageView, Uri uri);

  ImageView createImageView(Context context);

  ImageView createFakeImageView(Context context);
}
