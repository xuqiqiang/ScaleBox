package com.xuqiqiang.uikit.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class ViewUtils {
    private static Rect mViewRect;

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public static void setMargins(View v, int value) {
        setMargins(v, value, value, value, value);
    }

    public static void setMarginTop(View v, int t) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(p.leftMargin, t, p.rightMargin, p.bottomMargin);
            v.requestLayout();
        }
    }

    public static void setMarginBottom(View v, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(p.leftMargin, p.topMargin, p.rightMargin, b);
            v.requestLayout();
        }
    }

    public static void setMarginStart(View v, int s) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(s, p.topMargin, p.rightMargin, p.bottomMargin);
            v.requestLayout();
        }
    }

    public static void setMarginEnd(View v, int e) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(p.leftMargin, p.topMargin, e, p.bottomMargin);
            v.requestLayout();
        }
    }

    public static void setSize(View view, int width, int height) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = width;
        lp.height = height;
        view.setLayoutParams(lp);
    }

    public static Bitmap getViewBitmap(View view) {
        view.setDrawingCacheEnabled(false);
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * 没有在界面中看到的，要用layout布局一下才能获取图片
     * view.getDrawingCache()，默认最大为屏幕宽*高*4，超过了就会导致获得为空
     */
    public static Bitmap getViewBitmap2(View view) {
        //如果不调用这个方法，每次生成的bitmap相同
        view.setDrawingCacheEnabled(false);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap drawingCache = view.getDrawingCache();
        if (drawingCache == null) return null;
        return Bitmap.createBitmap(drawingCache);
    }

    public static Bitmap getViewBitmap(View view, int width, int height) {
        view.setDrawingCacheEnabled(false);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    public static Bitmap getViewBitmapAtMost(View view) {
        view.setDrawingCacheEnabled(false);
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(ScreenUtils.getWindowWidth(), View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(ScreenUtils.getWindowHeight(), View.MeasureSpec.AT_MOST));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    public static int fullScreenImmersive(Window window) {
        if (window == null) return -1;
        int systemUiVisibility = window.getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            window.getDecorView().setSystemUiVisibility(uiOptions);
        }
        return systemUiVisibility;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackgroundDrawable(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }

    public static void measureWidthAndHeight(View view) {
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
    }

    public static void doOnLayout(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                if (runnable != null)
                    runnable.run();
            }
        });
    }

    public static void addTopView(Activity activity, View view) {
//        Window window = activity.getWindow();
//        View decorView = window.getDecorView();
//        ViewGroup rootView = (FrameLayout) decorView.findViewById(android.R.id.content);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
//                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        rootView.addView(view, layoutParams);
        addTopView(activity, view, new FrameLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public static void addTopView(Activity activity, View view, FrameLayout.LayoutParams layoutParams) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        ViewGroup rootView = (FrameLayout) decorView.findViewById(android.R.id.content);
//        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams
//                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(view, layoutParams);
    }

    public static View addTopView(Activity activity, @LayoutRes int resource) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(resource, null);
        addTopView(activity, view);
        return view;
    }

    public static void removeView(View view) {
        if (view == null) return;
        ViewParent viewParent = view.getParent();
        if (viewParent instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) viewParent;
            viewGroup.removeView(view);
        }
    }

    public static boolean isInViewZone(View view, int x, int y) {
        if (null == mViewRect) {
            mViewRect = new Rect();
        }
        view.getDrawingRect(mViewRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mViewRect.left = location[0];
        mViewRect.top = location[1];
        mViewRect.right = mViewRect.right + location[0];
        mViewRect.bottom = mViewRect.bottom + location[1];
        return mViewRect.contains(x, y);
    }
}