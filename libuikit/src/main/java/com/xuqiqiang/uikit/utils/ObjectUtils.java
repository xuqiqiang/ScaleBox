package com.xuqiqiang.uikit.utils;

import java.util.Arrays;

/**
 * Created by xuqiqiang on 2020/01/02.
 */
public class ObjectUtils {

    /**
     * @see Object#equals(Object)
     */
    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * @see Arrays#deepEquals(Object[], Object[])
     * @see Objects#equals(Object, Object)
     */
    public static boolean deepEquals(Object a, Object b) {
        if (a == b)
            return true;
        else if (a == null || b == null)
            return false;
        else
            return _deepEquals(a, b);
    }

    private static boolean _deepEquals(Object e1, Object e2) {
        // BEGIN Android-changed: getComponentType() is faster than instanceof()
        Class<?> cl1 = e1.getClass().getComponentType();
        Class<?> cl2 = e2.getClass().getComponentType();

        if (cl1 != cl2) {
            return false;
        }
        if (e1 instanceof Object[])
            return Arrays.deepEquals((Object[]) e1, (Object[]) e2);
        else if (cl1 == byte.class)
            return Arrays.equals((byte[]) e1, (byte[]) e2);
        else if (cl1 == short.class)
            return Arrays.equals((short[]) e1, (short[]) e2);
        else if (cl1 == int.class)
            return Arrays.equals((int[]) e1, (int[]) e2);
        else if (cl1 == long.class)
            return Arrays.equals((long[]) e1, (long[]) e2);
        else if (cl1 == char.class)
            return Arrays.equals((char[]) e1, (char[]) e2);
        else if (cl1 == float.class)
            return Arrays.equals((float[]) e1, (float[]) e2);
        else if (cl1 == double.class)
            return Arrays.equals((double[]) e1, (double[]) e2);
        else if (cl1 == boolean.class)
            return Arrays.equals((boolean[]) e1, (boolean[]) e2);
        else
            return e1.equals(e2);
        // END Android-changed: getComponentType() is faster than instanceof()
    }

    /**
     * @see Object#hashCode
     */
    public static int hashCode(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    /**
     * @see Arrays#hashCode(Object[])
     */
    public static int hash(Object... a) {
        if (a == null)
            return 0;

        int result = 1;

        for (Object element : a)
            result = 31 * result + (element == null ? 0 : element.hashCode());

        return result;
    }

    public static String toString(Object o) {
        return String.valueOf(o);
    }

    public static String toString(Object o, String nullDefault) {
        return (o != null) ? o.toString() : nullDefault;
    }
}