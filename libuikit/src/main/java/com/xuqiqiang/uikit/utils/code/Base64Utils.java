package com.xuqiqiang.uikit.utils.code;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class Base64Utils {

    public static String encode(String str) {
        String str_64 = Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
        str_64 = str_64.replace("/", "_a");
        return str_64.trim();
    }

    public static String decode(String str_64) {
        str_64 = str_64.replace("_a", "/");
        String str = new String(
                Base64.decode(str_64.getBytes(), Base64.DEFAULT));
        return str.trim();
    }

    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
