package com.xuqiqiang.uikit.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Created by xuqiqiang on 2020/07/12.
 *
 * <pre><code>
 * {@literal @}Override protected void onStart() {
 *   super.onStart();
 *   PermissionUtils.checkPermission(this);
 * }
 * {@literal @}Override protected void onResume() {
 *   super.onResume();
 *   if (PermissionUtils.isPermissionGranted(this)) {
 *     ...
 *   }
 * }
 * </code></pre>
 */
public class PermissionUtils {

    public static final int REQUEST_CODE = 0xFF9999;

    private static String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static void initialize(String... permissions) {
        PermissionUtils.permissions = permissions;
    }

    public static boolean checkPermission(Activity activity) {
        if (isPermissionGranted(activity)) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
            return false;
        }
    }

    public static boolean isPermissionGranted(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissions) {
                int checkPermission = ContextCompat.checkSelfPermission(activity, permission);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
