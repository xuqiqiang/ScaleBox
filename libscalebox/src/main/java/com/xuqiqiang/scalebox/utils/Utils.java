package com.xuqiqiang.scalebox.utils;

import android.content.Context;
import android.util.Log;

import java.util.Arrays;

public class Utils {

    public static float dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return dipValue * scale + 0.5f;
    }
}
