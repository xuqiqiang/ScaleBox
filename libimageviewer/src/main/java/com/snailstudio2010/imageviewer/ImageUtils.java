package com.snailstudio2010.imageviewer;

import android.content.Context;

import com.xuqiqiang.uikit.utils.DisplayUtils;
import com.xuqiqiang.uikit.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import indi.liyi.viewer.ImageViewer;
import indi.liyi.viewer.ViewData;

/**
 * Created by xuqiqiang on 2019/09/04.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    public static boolean showImage(Context context, ImageViewer imageViewer, ImageInfo imageInfo) {
        return showImage(context, imageViewer, imageInfo, 0, 0, null);
    }

    public static boolean showImage(Context context, ImageViewer imageViewer, ImageInfo imageInfo,
                                    int imageWidth, int imageHeight, PhotoLoader photoLoader) {
//        if (imageInfo.getShowScene() != scene) return;
        final List<ViewData> list = new ArrayList<>();
        for (ImageInfo.Info info : imageInfo.getList()) {
            ViewData data = new ViewData(info.getSrc());
            if (!imageInfo.isShowDown() || info.getY() > 0) {
                data.setTargetX(info.getX());
                data.setTargetY(info.getY());
                data.setTargetWidth((int) info.getWidth());
                data.setTargetHeight((int) info.getHeight() + 1); //
            } else {
                data.setTargetX((int) DisplayUtils.dip2px(context, 15));
                data.setTargetY(ScreenUtils.getHeight());
                data.setTargetWidth(ScreenUtils.getWidth() -
                        (int) DisplayUtils.dip2px(context, 30));
                data.setTargetHeight((int) DisplayUtils.dip2px(context, info.getHeight()));
            }

            list.add(data);
        }

        try {
            imageViewer.overlayStatusBar(true)
                    .viewData(list)
                    .imageLoader(photoLoader != null ? photoLoader : new PhotoLoader())
                    .playEnterAnim(true)
                    .playExitAnim(true)
                    .showIndex(imageInfo.isShowIndex())
                    .watch(imageInfo.getIndex(), imageWidth, imageHeight);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
