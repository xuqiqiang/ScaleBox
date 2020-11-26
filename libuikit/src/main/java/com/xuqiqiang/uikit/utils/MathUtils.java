package com.xuqiqiang.uikit.utils;

import java.math.BigDecimal;

/**
 * Created by xuqiqiang on 2019/11/13.
 */
public class MathUtils {

    public static boolean eq0(float number) {
        return number == 0f || number > -0.000001 && number < 0.000001;
    }

    public static boolean eq0(double number) {
        return number == 0d || number > -0.000001 && number < 0.000001;
    }

    public static double scale(double number, int scale) {
        BigDecimal bg = new BigDecimal(number);
        double f1 = bg.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        System.out.println(f1);
        return f1;
    }
}