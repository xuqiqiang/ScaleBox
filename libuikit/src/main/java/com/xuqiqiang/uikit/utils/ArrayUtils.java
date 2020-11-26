package com.xuqiqiang.uikit.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by xuqiqiang on 2019/05/17.
 */
public class ArrayUtils {

    public static <T> ArrayList<T> createList(T... elements) {
        return new ArrayList<>(Arrays.asList(elements));
    }

    public static <T> List<T> createList(int size, Creator<T> creator) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < size; i += 1) {
            result.add(creator.create(i));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(int size, Class<T> type, Creator<T> creator) {
        T[] arr = (T[]) Array.newInstance(type, size);
        for (int i = 0; i < size; i += 1) {
            arr[i] = creator.create(i);
        }
        return (T[]) arr;
    }

    public static <T> ArrayList<T> asList(T[] array) {
        if (array == null) return null;
        return new ArrayList<>(Arrays.asList(array));
    }

    public static <T> boolean isEmpty(T[] array) {
        return array == null || array.length <= 0;
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.size() <= 0;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.size() <= 0;
    }

    public static <T> int size(Collection<T> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static <T> int size(T[] array) {
        return array == null ? 0 : array.length;
    }

    public static int size(Map map) {
        return map == null ? 0 : map.size();
    }

    public static String join(CharSequence delimiter, String[] elements) {
        StringBuilder contact = new StringBuilder();
        if (elements != null) {
            for (int i = 0; i < elements.length; i++) {
                contact.append(elements[i]);
                if (i < elements.length - 1) contact.append(delimiter);
            }
        }
        return contact.toString();
    }

    public static <E> void add(Collection<E> collection, E o) {
        if (collection == null || o == null || collection.contains(o)) return;
        collection.add(o);
    }

    public static <E> void add(List<E> collection, int index, E o) {
        if (collection == null || o == null || collection.contains(o)) return;
        collection.add(index, o);
    }

    public static boolean remove(Collection collection, Object o) {
        if (collection != null && o != null && collection.contains(o)) {
            return collection.remove(0);
        }
        return false;
    }

    public static <E> E get(List<E> list, int index) {
        if (list == null || index < 0 || list.size() <= index) return null;
        return list.get(index);
    }

    public static <T> int indexOf(T[] arr, T t, int defaultIndex) {
        for (int i = 0; i < arr.length; i += 1) {
            if (ObjectUtils.equals(t, arr[i])) return i;
        }
        return defaultIndex;
    }

    public static <T> int indexOf(List<T> list, T t, int defaultIndex) {
        int index = list.indexOf(t);
        if (index < 0) index = defaultIndex;
        return index;
    }

    public static <E, T> List<T> convert(List<E> list, Converter<E> converter) {
        List<T> result = new ArrayList<>();
        if (isEmpty(list)) return result;
        for (E e : list) {
            Object o = converter.convert(e);
            if (o instanceof Collection) {
                result.addAll((Collection) o);
            } else {
                result.add((T) o);
            }
        }
        return result;
    }

    public static <E, T> List<T> convert(E[] arr, Converter<E> converter) {
        List<T> result = new ArrayList<>();
        if (isEmpty(arr)) return result;
        for (E e : arr) {
            Object o = converter.convert(e);
            if (o instanceof Collection) {
                result.addAll((Collection) o);
            } else {
                result.add((T) o);
            }
        }
        return result;
    }

    public static <T> T find(List<T> list, Comparator<? super T> c) {
        if (isEmpty(list)) return null;
        T result = list.get(0);
        for (int i = 1; i < list.size(); i += 1) {
//            result = c.select(result, list.get(i));
            T t = list.get(i);
            if (c.compare(result, t) > 0) result = t;
        }
        return result;
    }

    public static <T> T find(T[] arr, Comparator<? super T> c) {
        if (isEmpty(arr)) return null;
        T result = arr[0];
        for (int i = 1; i < arr.length; i += 1) {
            T t = arr[i];
            if (c.compare(result, t) > 0) result = t;
        }
        return result;
    }

    public static <T> int indexOf(List<T> list, Condition<T> c) {
        for (int i = 0; i < list.size(); i += 1) {
            if (c.content(list.get(i))) return i;
        }
        return -1;
    }

    public static <T> int indexOf(T[] arr, Condition<T> c) {
        for (int i = 0; i < arr.length; i += 1) {
            if (c.content(arr[i])) return i;
        }
        return -1;
    }

    public interface Converter<E> {
        Object convert(E e);
    }

    public interface Creator<T> {
        T create(int index);
    }

    public interface Condition<T> {
        boolean content(T t);
    }

//    public interface Comparator<T> {
//        T select(T t1, T t2);
//    }
}