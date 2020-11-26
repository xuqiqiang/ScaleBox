package com.xuqiqiang.uikit.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.xuqiqiang.uikit.BuildConfig;

/**
 * ScreenInfo.java Use this class to get the information of the screen.
 * <p>
 * Created by xuqiqiang on 2016/05/17.
 */
public class ScreenUtils {
    private static final String TAG = ScreenUtils.class.getSimpleName();
    private static boolean initialized;

    private static int width;
    private static int height;

    private static int windowWidth;
    private static int windowHeight;

    /**
     * 判断是否是全面屏
     */
    private volatile static boolean mHasCheckAllScreen;
    private volatile static boolean mIsAllScreenDevice;

    public static boolean initialize(Activity activity) {
        if (initialized) return false;
        initialized = true;

        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        android.graphics.Point point = new android.graphics.Point();
        display.getRealSize(point);
        ScreenUtils.width = point.x;
        ScreenUtils.height = point.y;

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenUtils.windowWidth = dm.widthPixels;
        ScreenUtils.windowHeight = dm.heightPixels;

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "width:" + width);
            Log.i(TAG, "height:" + height);
            Log.i(TAG, "windowWidth:" + windowWidth);
            Log.i(TAG, "viewportHeight:" + windowHeight);
            Log.i(TAG, "statusBarHeight:" + StatusBarUtils.getStatusBarHeight(activity));
            Log.i(TAG, "navigationBarHeight:" + getNavigationBarHeight(activity));
            Log.i(TAG, "isAllScreenDevice:" + isAllScreenDevice(activity));
            Log.i(TAG, "navigationGestureEnabled:" + navigationGestureEnabled(activity));
        }
        return true;
    }


    public static int getWidthPixels() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return width;
    }

    /**
     * @return the number of pixel in the width of the screen.
     */
    public static int getWidth() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return width;
    }


    public static int getHeightPixels() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return height;
    }

    /**
     * @return the number of pixel in the height of the screen.
     */
    public static int getHeight() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return height;
    }

    public static int getWindowWidth() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return windowWidth;
    }

    public static int getWindowHeight() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return windowHeight;
    }

    public static int getViewportWidth() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return width;
    }

    public static int getViewportHeight(Activity activity) {
        if (!initialized) throw new RuntimeException("Not initialized");
        int statusBarHeight = StatusBarUtils.getStatusBarHeight(activity);
        int navigationBarHeight = getNavigationBarHeightIfRoom(activity);
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "height:" + height);
            Log.i(TAG, "statusBarHeight:" + statusBarHeight);
            Log.i(TAG, "navigationBarHeight:" + navigationBarHeight);
        }
        return height - statusBarHeight - navigationBarHeight;
    }

    public static String getSize() {
        if (!initialized) throw new RuntimeException("Not initialized");
        return width + "×" + height;
    }


//    public static int getNavigationHeight(Context context) {
//        int result = getNavigationBarHeightIfRoom(context);
//        Logger.d("getNavigationBarHeight:" + result);
//        return result;
//    }

    /**
     * 获取虚拟按键的高度
     * 1. 全面屏下
     * 1.1 开启全面屏开关-返回0
     * 1.2 关闭全面屏开关-执行非全面屏下处理方式
     * 2. 非全面屏下
     * 2.1 没有虚拟键-返回0
     * 2.1 虚拟键隐藏-返回0
     * 2.2 虚拟键存在且未隐藏-返回虚拟键实际高度
     */
    public static int getNavigationBarHeightIfRoom(Activity activity) {
        if (navigationGestureEnabled(activity)) {
            return 0;
        }
        return getCurrentNavigationBarHeight(activity);
    }

    /**
     * 全面屏（是否开启全面屏开关 0 关闭  1 开启）
     *
     * @param context
     * @return
     */
    private static boolean navigationGestureEnabled(Context context) {
        int val = Settings.Global.getInt(context.getContentResolver(), getDeviceInfo(), 0);
        return val != 0;
    }

    /**
     * 获取设备信息（目前支持几大主流的全面屏手机，亲测华为、小米、oppo、魅族、vivo都可以）
     *
     * @return
     */
    private static String getDeviceInfo() {
        String brand = Build.BRAND;
        if (TextUtils.isEmpty(brand)) return "navigationbar_is_min";

        if (brand.equalsIgnoreCase("HUAWEI")) {
            return "navigationbar_is_min";
        } else if (brand.equalsIgnoreCase("XIAOMI")) {
            return "force_fsg_nav_bar";
        } else if (brand.equalsIgnoreCase("VIVO")) {
            return "navigation_gesture_on";
        } else if (brand.equalsIgnoreCase("OPPO")) {
            return "navigation_gesture_on";
        } else {
            return "navigationbar_is_min";
        }
    }

    /**
     * 非全面屏下 虚拟键实际高度(隐藏后高度为0)
     *
     * @param activity
     * @return
     */
    private static int getCurrentNavigationBarHeight(Activity activity) {
        if (isNavigationBarShown(activity)) {
            return getNavigationBarHeight(activity);
        } else {
            return 0;
        }
    }

    /**
     * 非全面屏下 虚拟按键是否打开
     *
     * @param activity
     * @return
     */
    private static boolean isNavigationBarShown(Activity activity) {
        //虚拟键的view,为空或者不可见时是隐藏状态
        View view = activity.findViewById(android.R.id.navigationBarBackground);
        if (view == null) {
            return false;
        }
        int visible = view.getVisibility();
        if (visible == View.GONE || visible == View.INVISIBLE) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 非全面屏下 虚拟键高度(无论是否隐藏)
     *
     * @param context
     * @return
     */
    private static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isAllScreenDevice(Context context) {
        if (mHasCheckAllScreen) {
            return mIsAllScreenDevice;
        }
        mHasCheckAllScreen = true;
        mIsAllScreenDevice = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            android.graphics.Point point = new android.graphics.Point();
            display.getRealSize(point);
            float width, height;
            if (point.x < point.y) {
                width = point.x;
                height = point.y;
            } else {
                width = point.y;
                height = point.x;
            }
            if (height / width >= 1.97f) {
                mIsAllScreenDevice = true;
            }
        }
        return mIsAllScreenDevice;
    }
}