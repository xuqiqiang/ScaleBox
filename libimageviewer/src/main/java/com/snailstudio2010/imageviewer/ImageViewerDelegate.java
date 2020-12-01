package com.snailstudio2010.imageviewer;

import android.view.View;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public interface ImageViewerDelegate {

    void showImage(Object src);

    void showImage(Object src, View view);

    void showImage(Object[] srcArray, int index, View view);

    void showImagesByDialog(ImageInfo imageInfo);

    void showImagesByDialog(ImageInfo imageInfo, Runnable onShow, Runnable onHide);

    void showImageByDialog(Object src);

    void showImageByDialog(Object src, View view);

    void showImageByDialog(Object[] srcArray, int index, View view);
}
