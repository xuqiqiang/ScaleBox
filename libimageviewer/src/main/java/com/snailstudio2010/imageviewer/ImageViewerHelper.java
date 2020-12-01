package com.snailstudio2010.imageviewer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.utils.StatusBarUtils;
import com.xuqiqiang.uikit.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import indi.liyi.viewer.ImageViewer;
import indi.liyi.viewer.R;
import indi.liyi.viewer.ViewerStatus;
import indi.liyi.viewer.listener.OnBrowseStatusListener;
import indi.liyi.viewer.otherui.DefaultIndexUI;

import static com.xuqiqiang.uikit.utils.DisplayUtils.attrData;

/**
 * Created by xuqiqiang on 2020/08/19.
 */
public class ImageViewerHelper implements ImageViewerDelegate {

    private static final String TAG = ImageViewerHelper.class.getSimpleName();
    private final Activity mContext;
    ImageInfo mImageInfo;
    private ImageViewer mImageViewer;
    private boolean isImageShowing;
    private boolean isBackgroundDark = true;

    public ImageViewerHelper(Activity activity) {
        this.mContext = activity;
    }

    public boolean isImageShowing() {
        return isImageShowing;
    }

    public void showImages(ImageInfo imageInfo) {
        mImageInfo = imageInfo;
        if (mImageViewer == null) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mImageViewer == null) {
                        Window window = mContext.getWindow();
                        View decorView = window.getDecorView();
                        ViewGroup rootView = (FrameLayout) decorView.findViewById(android.R.id.content);

                        mImageViewer = initImageViewer();
                        mImageViewer.setOnBrowseStatusListener(new OnBrowseStatusListener() {
                            @Override
                            public void onBrowseStatus(int status) {
                                Log.d(TAG, "onBrowseStatus:" + status);
                                if (status == ViewerStatus.STATUS_SILENCE) {
                                    isImageShowing = false;
                                }
                            }
                        });
                        rootView.addView(mImageViewer);
                    }
                    mImageViewer.post(new Runnable() {
                        @Override
                        public void run() {
                            isImageShowing = ImageUtils.showImage(mContext, mImageViewer, mImageInfo);
                        }
                    });
                }
            });
        } else {
            mImageViewer.post(new Runnable() {
                @Override
                public void run() {
                    isImageShowing = ImageUtils.showImage(mContext, mImageViewer, mImageInfo);
                }
            });
        }
    }

    public void setBackgroundDark(boolean backgroundDark) {
        isBackgroundDark = backgroundDark;
    }

    protected ImageViewer initImageViewer() {
        ImageViewer imageViewer = new ImageViewer(mContext);
        // for test
        imageViewer.setBackgroundColor(attrData(mContext, android.R.attr.windowBackground));
        if (!isBackgroundDark) {
            imageViewer.loadIndexUI(new DefaultIndexUI(true) {

                @Override
                public View createView(Context context) {
                    View view = super.createView(context);
                    ((TextView) view).setTextColor(0xFF333333);
                    return view;
                }
            });
        }
        return imageViewer;
    }

    @Override
    public void showImage(Object src) {
        showImage(src, null);
    }

    @Override
    public void showImage(Object src, View view) {
        showImage(src == null ? null : new Object[]{src}, 0, view);
    }

    @Override
    public void showImage(final Object[] srcArray, final int index, final View view) {
        if (view == null) {
            showImages(initImageInfo(srcArray, index, null));
            return;
        }
        view.post(new Runnable() {
            @Override
            public void run() {
                showImages(initImageInfo(srcArray, index, view));
            }
        });
    }

    public ImageInfo initImageInfo(@Nullable Object[] srcArray, int index, @Nullable View view) {
        int[] pos = new int[2];
//        if (view != null) view.getLocationInWindow(pos);
        if (view != null) view.getLocationOnScreen(pos);
        if (StatusBarUtils.getRootViewFitsSystemWindows(mContext))
            pos[1] -= StatusBarUtils.getStatusBarHeight(mContext);

        ImageInfo imageInfo = new ImageInfo();
        if (view == null) {
            imageInfo.setShowDown(true);
        }
        if (srcArray == null || srcArray.length == 1) {
            ImageInfo.Info info = new ImageInfo.Info();
            info.setSrc(srcArray != null ? srcArray[0] :
                    ViewUtils.getViewBitmap(view, view.getWidth(), view.getHeight()));
            if (view != null) {
                info.setX(pos[0]);
                info.setY(pos[1]);// - StatusBarUtils.getStatusBarHeight(this));
                info.setWidth(view.getWidth());
                info.setHeight(view.getHeight());
            }
            imageInfo.setList(ArrayUtils.createList(info));
        } else {
            List<ImageInfo.Info> list = new ArrayList<>();
            for (Object src : srcArray) {
                ImageInfo.Info info = new ImageInfo.Info();
                info.setSrc(src);
                if (view != null) {
                    info.setX(pos[0]);
                    info.setY(pos[1]);// - StatusBarUtils.getStatusBarHeight(this));
                    info.setWidth(view.getWidth());
                    info.setHeight(view.getHeight());
                }
                list.add(info);
            }
            imageInfo.setList(list);

            imageInfo.setIndex(index >= 0 && index < srcArray.length ? index : 0);
            imageInfo.setShowIndex(true);
        }
        return imageInfo;
    }

    @Override
    public void showImagesByDialog(ImageInfo imageInfo) {
        showImagesByDialog(imageInfo, null, null);
    }

    @Override
    public void showImagesByDialog(ImageInfo imageInfo, final Runnable onShow, final Runnable onHide) {
        mImageInfo = imageInfo;

        final Dialog dialog = new Dialog(mContext, R.style.CustomDialog_Transparent);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final ImageViewer mImageViewer = new ImageViewer(mContext);
        mImageViewer.setBackgroundColor(Color.TRANSPARENT);
//        ImageViewer mImageViewer = initImageViewer();
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    mImageViewer.cancel();
                }
                return false;
            }
        });

        mImageViewer.setOnBrowseStatusListener(new OnBrowseStatusListener() {
            @Override
            public void onBrowseStatus(int status) {
                Log.d(TAG, "onBrowseStatus:" + status);
                if (status == ViewerStatus.STATUS_SILENCE) {
                    dialog.dismiss();
                    if (onHide != null) onHide.run();
                    isImageShowing = false;
//                mImageViewer.postDelayed(dialog::dismiss, 500);
                }
            }
        });
        dialog.setContentView(mImageViewer);
        dialog.getWindow().setWindowAnimations(R.style.AnimationNone);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();

        mImageViewer.post(new Runnable() {
            @Override
            public void run() {
                isImageShowing = ImageUtils.showImage(mContext, mImageViewer, mImageInfo);
                if (onShow != null) onShow.run();
            }
        });
    }

    @Override
    public void showImageByDialog(Object src) {
        showImageByDialog(src, null);
    }

    @Override
    public void showImageByDialog(Object src, View view) {
        showImageByDialog(src == null ? null : new Object[]{src}, 0, view);
    }

    @Override
    public void showImageByDialog(final Object[] srcArray, final int index, final View view) {
        if (view == null) {
            showImagesByDialog(initImageInfo(srcArray, index, null));
            return;
        }
        view.post(new Runnable() {
            @Override
            public void run() {
                showImagesByDialog(initImageInfo(srcArray, index, view),
                        new Runnable() {
                            @Override
                            public void run() {
                                view.setVisibility(View.INVISIBLE);
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                view.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });
    }

    public boolean onBackPressed() {
        return mImageViewer != null && mImageViewer.onBackPressed();
    }
}
