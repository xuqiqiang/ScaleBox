package com.xuqiqiang.uikit.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public final class TimeUtils {

    private static SimpleDateFormat sdf;

    private TimeUtils() {
    }

    public static String formatCurrentDate(String separator) {
        return formatDate(System.currentTimeMillis(), separator);
    }

    public static String formatDate(long l) {
        return formatTime(l, "yyyy-MM-dd");
    }

    public static String formatDate(long l, String separator) {
        return formatTime(l, "yyyy-MM-dd".replace("-", separator));
    }

    public static String formatCurrentTime(String strPattern) {
        return formatTime(System.currentTimeMillis(), strPattern);
    }

    public static String formatTime(long l, String strPattern) {
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }

    public static String format(long l, String datePattern, String timePattern) {
        long now = System.currentTimeMillis() / 1000L;
        long daySecond = 60 * 60 * 24;
        long dayTime = now - (now + 8 * 3600) % daySecond;

        if (l / 1000L < dayTime) return formatTime(l, datePattern + " " + timePattern);
        else return formatTime(l, timePattern);
    }

    public static String formatSimple(long l, String datePattern, String timePattern) {
        long now = System.currentTimeMillis() / 1000L;
        long daySecond = 60 * 60 * 24;
        long dayTime = now - (now + 8 * 3600) % daySecond;

        if (l / 1000L < dayTime - daySecond * 1000L)
            return formatTime(l, datePattern + " " + timePattern);
        else if (l / 1000L < dayTime) return "昨天";
        else return formatTime(l, timePattern);
    }

    public static long parseTime(String time, String strPattern) {
        if (TextUtils.isEmpty(time))
            return 0;
        SimpleDateFormat format = new SimpleDateFormat(strPattern);
        try {
            Date date = format.parse(time);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
