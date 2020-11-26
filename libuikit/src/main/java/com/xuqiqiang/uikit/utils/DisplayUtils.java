package com.xuqiqiang.uikit.utils;

import android.content.Context;
import androidx.annotation.AttrRes;
import android.util.TypedValue;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class DisplayUtils {
    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param context
     * @param pxValue （DisplayMetrics类中属性density）
     * @return
     */
    public static float px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return pxValue / scale + 0.5f;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param context
     * @param dipValue （DisplayMetrics类中属性density）
     * @return
     */
    public static float dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param context
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static float px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale + 0.5f;
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static float sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return spValue * fontScale + 0.5f;
    }

    public static float px2dip(float pxValue) {
        return px2dip(Utils.getApp(), pxValue);
    }

    public static float dip2px(float dipValue) {
        return dip2px(Utils.getApp(), dipValue);
    }

    public static float px2sp(float pxValue) {
        return px2sp(Utils.getApp(), pxValue);
    }

    public static float sp2px(float spValue) {
        return sp2px(Utils.getApp(), spValue);
    }

    public static int attrData(Context context, @AttrRes int resId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public static int attrResId(Context context, @AttrRes int resid) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resid, typedValue, true);
        return typedValue.resourceId;
    }
}