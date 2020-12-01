package com.snailstudio2010.imageviewer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.xuqiqiang.uikit.activity.BaseAppActivity;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.view.menu.PopupMenu;

import indi.liyi.viewer.ImageViewer;
import indi.liyi.viewer.listener.OnItemLongPressListener;

/**
 * Created by xuqiqiang on 2020/08/19.
 */
public abstract class ImageViewerActivity extends BaseAppActivity implements ImageViewerDelegate {

    private ImageViewerHelper mImageViewerHelper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageViewerHelper = new ImageViewerHelper(this) {
            @Override
            protected ImageViewer initImageViewer() {
                ImageViewer imageViewer = super.initImageViewer();
                imageViewer.setOnItemLongPressListener(new OnItemLongPressListener() {
                    @Override
                    public boolean onItemLongPress(int position, ImageView imageView) {
                        PopupMenu popupMenu = initImageMenu(mImageInfo.getList().get(position));
                        if (popupMenu != null) {
                            popupMenu.show();
                        }
                        return true;
                    }
                });
                return imageViewer;
            }
        };
        mImageViewerHelper.setBackgroundDark(isBackgroundDark());
    }

    public PopupMenu initImageMenu(ImageInfo.Info info) {
        return null;
    }

    protected boolean isBackgroundDark() {
        return true;
    }

    @Override
    public void showImage(Object src) {
        mImageViewerHelper.showImage(src);
    }

    @Override
    public void showImage(Object src, View view) {
        mImageViewerHelper.showImage(src, view);
    }

    @Override
    public void showImage(final Object[] srcArray, final int index, final View view) {
        mImageViewerHelper.showImage(srcArray, index, view);
    }

    @Override
    public void showImagesByDialog(ImageInfo imageInfo) {
        mImageViewerHelper.showImagesByDialog(imageInfo);
    }

    @Override
    public void showImagesByDialog(ImageInfo imageInfo, final Runnable onShow, final Runnable onHide) {
        mImageViewerHelper.showImagesByDialog(imageInfo, onShow, onHide);
    }

    @Override
    public void showImageByDialog(Object src) {
        mImageViewerHelper.showImageByDialog(src);
    }

    @Override
    public void showImageByDialog(Object src, View view) {
        mImageViewerHelper.showImageByDialog(src, view);
    }

    @Override
    public void showImageByDialog(final Object[] srcArray, final int index, final View view) {
        mImageViewerHelper.showImageByDialog(srcArray, index, view);
    }

    @Override
    public void onBackPressed() {
        if (mImageViewerHelper != null && mImageViewerHelper.onBackPressed()) return;
        super.onBackPressed();
    }
}
