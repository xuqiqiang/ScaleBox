package com.xuqiqiang.scalebox.demo.utils;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Environment;
import android.widget.ImageView;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.utils.Logger;

import java.io.File;

/**
 * Created by xuqiqiang on 2019/08/20.
 */
public class FrescoImageLoader implements ImageLoader {

    private static final String TAG = "FrescoImageLoader";
    private static FrescoImageLoader mInstance;

    private FrescoImageLoader(Context context) {
        String rootName = getStoreDir(context).getPath()
                + File.separator + "scalebox";
        DiskCacheConfig diskCacheConfig =
                DiskCacheConfig
                        .newBuilder(context)
                        .setBaseDirectoryPath(new File(rootName))
                        .build();

        ImagePipelineConfig config =
                ImagePipelineConfig
                        .newBuilder(context)
                        .setDownsampleEnabled(true).setMainDiskCacheConfig(diskCacheConfig)
                        .build();
        Fresco.initialize(context, config);
    }

    private static File getStoreDir(Context context) {
        File dataDir = null;
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment
                .getExternalStorageState())) {
            dataDir = Environment.getExternalStorageDirectory();
        } else {
            dataDir = context.getApplicationContext().getFilesDir();
        }
        return dataDir;
    }

    public static void initialize(Context context) {
        if (mInstance == null) {
            mInstance = new FrescoImageLoader(context.getApplicationContext());
        }
    }

    public static FrescoImageLoader getInstance() {
        return mInstance;
    }

    @Override
    public void bindImage(final ImageView photoImageView, Uri uri, int width, int height) {
        DraweeView draweeView = (DraweeView) photoImageView;
        final ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(uri);
        if (width > 0 && height > 0) {
            requestBuilder.setResizeOptions(new ResizeOptions(width, height));
        } else {
            width = draweeView.getWidth();
            height = draweeView.getHeight();
            if (width > 0 && height > 0) {
                requestBuilder.setResizeOptions(new ResizeOptions(width, height));
            }
        }
        photoImageView.setTag(R.id.tag_image_loaded, null);
        ImageRequest imageRequest = requestBuilder.build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(draweeView.getController())
                .setControllerListener(new ControllerListener<ImageInfo>() {

                    @Override
                    public void onSubmit(String id, Object callerContext) {
                    }

                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                        Logger.d("size:" + imageInfo.getWidth() + "," + imageInfo.getHeight());
                        photoImageView.setTag(new int[]{imageInfo.getWidth(), imageInfo.getHeight()});
                        photoImageView.setTag(R.id.tag_image_loaded, true);
                    }

                    @Override
                    public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
                    }

                    @Override
                    public void onIntermediateImageFailed(String id, Throwable throwable) {
                    }

                    @Override
                    public void onFailure(String id, Throwable throwable) {
                    }

                    @Override
                    public void onRelease(String id) {
                    }
                })
                .setImageRequest(imageRequest).build();
        draweeView.setController(controller);
    }

    @Override
    public void bindImage(ImageView imageView, Uri uri) {
        bindImage(imageView, uri, 0, 0);
    }

    @Override
    public ImageView createImageView(Context context) {
        SimpleDraweeView simpleDraweeView = new SimpleDraweeView(context);
        return simpleDraweeView;
    }

    @Override
    public ImageView createFakeImageView(Context context) {
        SimpleDraweeView fakeImage = new SimpleDraweeView(context);
        fakeImage.getHierarchy().setActualImageScaleType(ScaleTypeFillCenterInside.INSTANCE);
        return fakeImage;
    }

    static class ScaleTypeFillCenterInside extends ScalingUtils.AbstractScaleType {

        public static final ScalingUtils.ScaleType INSTANCE = new ScaleTypeFillCenterInside();

        @Override
        public void getTransformImpl(
                Matrix outTransform,
                Rect parentRect,
                int childWidth,
                int childHeight,
                float focusX,
                float focusY,
                float scaleX,
                float scaleY) {
            float scale = Math.min(scaleX, scaleY);
            float dx = parentRect.left + (parentRect.width() - childWidth * scale) * 0.5f;
            float dy = parentRect.top + (parentRect.height() - childHeight * scale) * 0.5f;
            outTransform.setScale(scale, scale);
            outTransform.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        }
    }
}