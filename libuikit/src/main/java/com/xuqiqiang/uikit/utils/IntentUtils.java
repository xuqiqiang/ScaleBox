/**
 * Copyright (C) 2016 Snailstudio. All rights reserved.
 * <p>
 * https://xuqiqiang.github.io/
 *
 * @author xuqiqiang (the sole member of Snailstudio)
 */
package com.xuqiqiang.uikit.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.xuqiqiang.uikit.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class IntentUtils {

    public static final String PACKAGE_WECHAT = "com.tencent.mm";
    public static final String PACKAGE_MOBILE_QQ = "com.tencent.mobileqq";
    /**
     * 微信7.0版本号，兼容处理微信7.0版本分享到朋友圈不支持多图片的问题
     */
    public static final int VERSION_CODE_FOR_WEI_XIN_VER7 = 1380;

    public static final String camera_photo_path = ".camera_photo";
    public static final String camera_photo_name = "camera_photo.jpg";
    public static final int DEFAULT_REQUEST_CODE = 99;
    public static final String PHOTO_CROP_PATH = ".PhotoCrop";
    public static final String PHOTO_CROP_FILE_NAME = "photo_cropped.jpg";

    private static final String TAG = IntentUtils.class.getSimpleName();

    public static File getPhotographFile() {
        return new File(Cache.getRealFilePath(camera_photo_path),
                camera_photo_name);
    }

    public static boolean takePhotograph(Activity context, int requestCode) {
        try {
//            Cache.createDir(camera_photo_path);
            FileUtils.createDir(camera_photo_path);

            Intent intent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getPhotographFile()));
            context.startActivityForResult(intent,
                    requestCode);
        } catch (ActivityNotFoundException e) {
//            ToastMaster.showToast(context, R.string.not_install_app);
            return false;
        }
        return true;
    }

    public static boolean getImageFromGallery(Activity context, int requestCode) {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            context.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    public static File getCropFile() {
        return new File(
                Cache.getRealFilePath(PHOTO_CROP_PATH),
                PHOTO_CROP_FILE_NAME);
    }

    /**
     * 调用系统图片编辑进行裁剪
     */
    public static boolean startPhotoCrop(Activity context, Uri uri, int requestCode) {

        try {
            FileUtils.createDir(PHOTO_CROP_PATH);

            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCropFile()));
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat",
                    Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true); // no face detection
            context.startActivityForResult(intent, requestCode);

        } catch (ActivityNotFoundException e) {
            return false;
        }
        return true;
    }

    public static void showImage(Activity context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        context.startActivity(intent);
    }

    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null,
                null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file
            // path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor
                    .getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        Log.d(TAG, "file path:" + result);
        return result;
    }

    public static void getLocalImage(Activity context, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        context.startActivityForResult(intent, requestCode);
    }

    public static boolean checkPermission(Context context, String permission) {
//        Log.d(TAG, "checkSelfPermission:" + context.checkSelfPermission(
//                permission));
//        Log.d(TAG, "checkCallingOrSelfPermission:" + context.checkCallingOrSelfPermission(
//                permission));
//        Log.d(TAG, "checkPermission:" + context.checkPermission(
//                permission,
//                android.os.Process.myPid(), android.os.Process.myUid()));
        return context.checkPermission(
                permission,
                android.os.Process.myPid(), android.os.Process.myUid())
                == PackageManager.PERMISSION_GRANTED;
    }

//    public boolean selfPermissionGranted(String permission) {
//        // For Android < Android M, self permissions are always granted.
//        boolean result = true;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            if (targetSdkVersion >= Build.VERSION_CODES.M) {
//                // targetSdkVersion >= Android M, we can
//                // use Context#checkSelfPermission
//                result = context.checkSelfPermission(permission)
//                        == PackageManager.PERMISSION_GRANTED;
//            } else {
//                // targetSdkVersion < Android M, we have to use PermissionChecker
//                result = PermissionChecker.checkSelfPermission(context, permission)
//                        == PermissionChecker.PERMISSION_GRANTED;
//            }
//        }
//
//        return result;
//    }

    public static void requestPermissions(Activity activity,
                                          String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                ReflectionUtils.getMethod(activity.getClass(),
                        "validateRequestPermissionsRequestCode",
                        int.class).invoke(DEFAULT_REQUEST_CODE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            activity.requestPermissions(new String[]{
                    permission}, DEFAULT_REQUEST_CODE);

        }
//        } else {
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    final int[] grantResults = new int[permissions.length];
//
//                    PackageManager packageManager = activity.getPackageManager();
//                    String packageName = activity.getPackageName();
//
//                    final int permissionCount = permissions.length;
//                    for (int i = 0; i < permissionCount; i++) {
//                        grantResults[i] = packageManager.checkPermission(
//                                permissions[i], packageName);
//                    }
//
//                }
//            });
//        }
    }

    public static void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                  int[] grantResults) {
        if (requestCode == DEFAULT_REQUEST_CODE) {

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean checkAndRequestPermissions(Activity activity, String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        List<String> lackedPermission = new ArrayList<>();
        for (String permission : permissions) {
            if (!(activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)) {
                lackedPermission.add(permission);
            }
        }
//        if (!(activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
//            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
//        }
//
//        if (!(activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
//            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//
//        if (!(activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
//            lackedPermission.add(Manifest.permission.ACCESS_FINE_LOCATION);
//        }

        if (lackedPermission.size() == 0) {
            return true;
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            activity.requestPermissions(requestPermissions, DEFAULT_REQUEST_CODE);
        }
        return false;
    }

    public static void runOnUiThread(final Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (runnable != null) runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void runOnUiThread(final Runnable runnable, Handler handler) {
        if (handler != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (runnable != null) runnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            runOnUiThread(runnable);
        }
    }

    public static void runOnUiThread(final Runnable runnable, Activity activity) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (runnable != null) runnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            runOnUiThread(runnable);
        }
    }

    public static void copyText(Context context, String text) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clip = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clip != null)
                clip.setText(text);
        } else {
            ClipboardManager clip = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clip != null) {
                clip.setPrimaryClip(ClipData.newPlainText(null, text));
            }
        }
    }

    // region share

    public static boolean sharePic(Context context, String filePath) {
        return shareFile(context, filePath, "image/*");
    }

    public static boolean shareVideo(Context context, String filePath) {
        return shareFile(context, filePath, "video/*");
    }

    public static boolean shareFile(Context context, String filePath) {
        return shareFile(context, filePath, MimeUtils.getMIMEType(new File(filePath)));
    }

    public static boolean shareFile(Context context, String filePath, String type) {
        File shareFile = new File(filePath);
        if (!shareFile.exists()) return false;
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Uri contentUri = FileProvider.getUriForFile(context,
//                    context.getPackageName() + ".fileProvider", shareFile);
            Uri contentUri;
            try {
                contentUri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        shareFile.getAbsolutePath(), shareFile.getName(), null));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                contentUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileProvider", shareFile);
            }
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareFile));
        }
        intent.setType(type);
        Intent chooser = Intent.createChooser(intent, "分享到");
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(chooser);
        }
        return true;
    }

    public static void shareText(Context context, String text) {
//        Log.d(TAG, "shareText:" + content);
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("text/plain");
//        intent.putExtra(Intent.EXTRA_TEXT, content);
//        context.startActivity(Intent.createChooser(intent,
//                "分享到"));
        shareText(context, text, null);
    }

    public static void shareText(Context context, String text, ComponentName componentName) {
        Log.d(TAG, "shareText:" + text);
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (componentName != null) {
            intent.setComponent(componentName);
        }
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent,
                "分享到"));
    }

    /**
     * 分享文字到微信好友
     */
    public static boolean shareToWechat(Context context, String text) {
        if (!ApplicationUtils.isAppInstalled(context, PACKAGE_WECHAT)) return false;
        shareText(context, text, new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareImgUI"));
        return true;
    }

    /**
     * 分享文字到QQ好友
     */
    public static boolean shareToQQ(Context context, String text) {
        if (!ApplicationUtils.isAppInstalled(context, PACKAGE_MOBILE_QQ)) return false;
        shareText(context, text, new ComponentName(PACKAGE_MOBILE_QQ, "com.tencent.mobileqq.activity.JumpActivity"));
        return true;
    }

    /**
     * 分享多张图片
     */
    public static void shareImages(Context context, String text, File[] files, ComponentName componentName) {
        Intent intent = new Intent();
        if (componentName != null) {
            intent.setComponent(componentName);
        }
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        ArrayList<Uri> imageUris = new ArrayList<>();
        try {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int targetSDK = applicationInfo.targetSdkVersion;
            for (File f : files) {
                Uri u;
                if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                u = FileProvider.getUriForFile(context,
//                        context.getPackageName() + ".fileProvider", f);
                    u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            f.getAbsolutePath(), f.getName(), null));
                } else {
                    u = Uri.fromFile(f);
                }
                imageUris.add(u);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        intent.putExtra(Intent.EXTRA_SUBJECT, text);
//        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        try {
            context.startActivity(Intent.createChooser(intent, "Share"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分享多张图片
     */
    public static void shareImages(Context context, String text, File[] files) {
        if (ArrayUtils.isEmpty(files)) return;
        shareImages(context, text, files, null);
    }

    /**
     * 分享多张图片到微信好友
     */
    public static boolean shareToWechat(Context context, String text, File[] files) {
        if (!ApplicationUtils.isAppInstalled(context, PACKAGE_WECHAT)) return false;
        shareImages(context, text, files, new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareImgUI"));
        return true;
    }

    /**
     * 分享多张图片到QQ好友
     */
    public static boolean shareToQQ(Context context, String text, File[] files) {
        if (ArrayUtils.isEmpty(files)) return false;
        if (!ApplicationUtils.isAppInstalled(context, PACKAGE_MOBILE_QQ)) return false;
        shareImages(context, text, files, new ComponentName(PACKAGE_MOBILE_QQ, "com.tencent.mobileqq.activity.JumpActivity"));
        return true;
    }

    /**
     * 分享多张图片到朋友圈，微信7.0及以上版本不支持分享多张图片
     */
    public static boolean shareToCircle(Context context, String text, File[] files) {
        if (!ApplicationUtils.isAppInstalled(context, PACKAGE_WECHAT)) return false;

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        ComponentName comp = new ComponentName(PACKAGE_WECHAT, "com.tencent.mm.ui.tools.ShareToTimeLineUI");
        intent.setComponent(comp);
        intent.setType("image/*");
        if (ApplicationUtils.getVersionCode(context, PACKAGE_WECHAT) < VERSION_CODE_FOR_WEI_XIN_VER7) {
            ArrayList<Uri> uris = new ArrayList<>();
            try {
                ApplicationInfo applicationInfo = context.getApplicationInfo();
                int targetSDK = applicationInfo.targetSdkVersion;
                for (File f : files) {
                    Uri u;
                    if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(context.getContentResolver(),
                                f.getAbsolutePath(), f.getName(), null));
//                        u = FileProvider.getUriForFile(context,
//                                context.getPackageName() + ".fileProvider", f);
                    } else {
                        u = Uri.fromFile(f);
                    }
                    uris.add(u);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 微信7.0以下版本
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            Uri uri;
            try {
                ApplicationInfo applicationInfo = context.getApplicationInfo();
                int targetSDK = applicationInfo.targetSdkVersion;
                if (targetSDK >= Build.VERSION_CODES.N && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                    uri = FileProvider.getUriForFile(context,
//                            context.getPackageName() + ".fileProvider", files[0]);
                    uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            files[0].getAbsolutePath(), files[0].getName(), null));
                } else {
                    uri = Uri.fromFile(files[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                uri = Uri.fromFile(files[0]);
            }

            // 微信7.0及以上版本
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }

        intent.putExtra("Kdescription", text);
        intent.putExtra(Intent.EXTRA_SUBJECT, text);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            context.startActivity(Intent.createChooser(intent, "Share"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    // endregion

    // region Gallery

    public static void goToGallery(Context context, Uri uri) {
        if (uri == null) {
            Log.e(TAG, "uri is null");
            return;
        }

        if (tryGallery(context, uri)) return;

        File file = toFile(context, uri);
        if (file == null) {
            Log.e(TAG, "file is null");
            Toast.makeText(context, R.string.open_gallery_error, Toast.LENGTH_LONG).show();
            return;
        }

        Uri imageUri = getImageContentUri(context, file);
        if (imageUri != null && tryGallery(context, imageUri)) return;

        openFile(context, file);
    }

    private static boolean tryGallery(Context context, Uri uri) {

        try {
            Intent intent = new Intent(Intent.ACTION_MAIN).setClassName(
                    "com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity");
            intent.setAction(Intent.ACTION_VIEW);
            //intent.setDataAndType(uri,"image/*");
            intent.setData(uri);
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            ComponentName componentName = intent.resolveActivity(context.getPackageManager());
            if (componentName != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void openFile(Context context, File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            fixIntent(context, intent, file);
            context.startActivity(intent);
            Log.d(TAG, "openFile");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.open_gallery_error, Toast.LENGTH_LONG).show();
//            ToastMaster.showToast(context, "无法打开文件");
        }
    }

    public static File toFile(Context context, Uri uri) {
        Log.d(TAG, "toFile " + uri.getPath());
        String path = uri.getPath();
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists() && file.isFile()) return file;
        }
        String[] arr = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, arr, null, null, null);
        if (cursor == null) return null;
        try {
            int imgIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imgPath = cursor.getString(imgIndex);
            return new File(imgPath);
        } finally {
            cursor.close();
        }
    }


    public static Uri getImageContentUri(Context context, File imageFile) {
        try {
            String filePath = imageFile.getAbsolutePath();
            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                    new String[]{filePath}, null);
            if (cursor != null && cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                Uri baseUri = Uri.parse("content://media/external/images/media");
                return Uri.withAppendedPath(baseUri, "" + id);
            } else {
                if (imageFile.exists()) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, filePath);
                    return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Intent fixIntent(Context context, Intent intent, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.d(TAG, "openFile:" + context.getPackageName());
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, MimeUtils.getMIMEType(file));
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), MimeUtils.getMIMEType(file));
        }
        return intent;
    }
    // endregion
}