package com.xuqiqiang.uikit.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import com.xuqiqiang.uikit.utils.code.EncodingUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class Cache {
    private static final String TAG = Cache.class.getSimpleName();
    public static String rootName;
    public static String spName;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private FileInputStream fis;
    private DataInputStream dis;
    private FileOutputStream fos;
    private DataOutputStream dos;

    private Cache() {
    }

    public static void initialize(Context context, String name) {
        Cache.context = context.getApplicationContext();
//        Cache.name = name;
        setRootName(name);
        initSharedPreferences(name);
    }

    public static boolean hasInit() {
        return context != null;
    }

    public static Cache getInstance() {
        return new Cache();
    }

    public static void initSharedPreferences() {
        String spName = context.getPackageName();
        initSharedPreferences(spName);
    }

    public static void initSharedPreferences(String name) {
        if (editor != null) {
            editor.apply();
        }
        spName = name.replace(File.separator, "_");
        sharedPreferences = context.getSharedPreferences(spName,
                Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static void setRootName(String name) {
        if (!TextUtils.isEmpty(name)) {
            rootName = getStoreDir().getPath()
                    + (name.startsWith(File.separator) ? name : File.separator + name);
            createRootDir();
        } else
            rootName = getStoreDir().getPath();
    }

    public static File getFile(String[] dirName, String fileName) {
        return new File(getRealFilePath(dirName), fileName);
    }

    public static Bitmap readBitmap(String[] cacheName, String fileName) {
        File file = getFile(cacheName, fileName);
        if (!file.exists())
            return null;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        return bitmap;
    }

    public static Bitmap readBitmap(File file) {
        if (!file.exists())
            return null;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(file.getPath());
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // SharedPreferences
    //////////////////////////////////////////////////////////////////////////////////////////////
    public static int readInt(String name, int arg) {
        return sharedPreferences.getInt(name, arg);
    }

    public static void writeInt(String name, int a) {
        editor.putInt(name, a);
        editor.commit();
    }

    public static float readFloat(String name, float arg) {
        return sharedPreferences.getFloat(name, arg);
    }

    public static void writeFloat(String name, float a) {
        editor.putFloat(name, a);
        editor.commit();
    }

    public static double readDouble(String name, double arg) {
        double result = arg;
        try {
            String str = sharedPreferences.getString(name, arg + "");
            result = Double.valueOf(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    public static void writeDouble(String name, double a) {
        editor.putString(name, a + "");
        editor.commit();
    }

    public static String readString(String name, String arg) {
        return sharedPreferences.getString(name, arg);
    }

    public static void writeString(String name, String a) {
        editor.putString(name, a);
        editor.commit();
    }

    public static Boolean readBoolean(String name, Boolean arg) {
        return sharedPreferences.getBoolean(name, arg);
    }

    public static void writeBoolean(String name, Boolean a) {
        editor.putBoolean(name, a);
        editor.commit();
    }

    public static long readLong(String name, long arg) {
        return sharedPreferences.getLong(name, arg);
    }

    public static void writeLong(String name, long a) {
        editor.putLong(name, a);
        editor.commit();
    }

    public static void removeKey(String key) {
        editor.remove(key);
    }

    public static String getFileStr(String path) {
        InputStream in = null;
        byte[] data = null;
        // 读取图片字节数组
        try {
            in = new FileInputStream(path);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    // byte[] → Bitmap
    public static Bitmap bytes2Bitmap(byte[] b) {
        if (b == null || b.length == 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public static Bitmap str2Bitmap(String base64) {
        if (TextUtils.isEmpty(base64)) return null;
        if (base64.contains(",")) base64 = base64.split(",")[1];
        byte[] buffer = Base64.decode(base64, Base64.DEFAULT);
        return bytes2Bitmap(buffer);
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

    public static boolean copyBigDataToSD(String assetsFileName,
                                          String strOutFileName) throws IOException {
        if (!FileUtils.createFileByDeleteOldFile(strOutFileName)) return false;
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            myOutput = new FileOutputStream(strOutFileName);
            myInput = context.getAssets().open(assetsFileName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (myInput != null)
                    myInput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (myOutput != null)
                    myOutput.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 清除掉所有特殊字符，只允许字母和数字
    // String regEx = "[^a-zA-Z0-9]";
    public static String stringFilter(String str) {
        String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static boolean createRootDir() {
        return FileUtils.createFile(new File(rootName, ".nomedia"), false);
    }

    public static boolean createDir(String path) {
        return FileUtils.createFile(new File(path, ".nomedia"), false);
    }

    public static boolean hasCreateRootDir() {
        File rootFile = new File(rootName);
        File noMediaFile = new File(rootName, ".nomedia");
        return rootFile.exists() && rootFile.isDirectory() && noMediaFile.exists();
    }

    /**
     * get root directory
     *
     * @return
     */
    public static File getStoreDir() {
        File dataDir = null;
        if (Environment.MEDIA_MOUNTED.equalsIgnoreCase(Environment
                .getExternalStorageState())) {
            dataDir = Environment.getExternalStorageDirectory();
        } else {
            dataDir = context.getApplicationContext().getFilesDir();
        }
        return dataDir;
    }

    public static String createRealFilePath(String path) {
        String realFilePath = getRealFilePath(path);
        FileUtils.createDir(realFilePath);
        return realFilePath;
    }

    public static String getRealFilePath(String path) {
        createRootDir();
        if (path.startsWith(File.separator))
            return Cache.rootName + path;
        else
            return Cache.rootName + File.separator + path;
    }

    public static String getRealFilePath(String[] dirName) {
        StringBuilder path = new StringBuilder(rootName);
        if (dirName != null) {
            for (String name : dirName) {
                path.append(File.separator).append(name);
            }
        }
        return path.toString();
    }

    public static boolean hasSDCard() {
        return Environment.getExternalStorageState() != null
                && !Environment.getExternalStorageState().equals("removed");
    }

    // 递归方式 计算文件的大小
    public static long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }

    // 递归方式 获取所有文件
    public static void getAllFilesInDir(final File file, ArrayList<File> list) {
        if (file.isFile()) {
            list.add(file);
        } else {
            final File[] children = file.listFiles();
            if (children != null)
                for (final File child : children)
                    getAllFilesInDir(child, list);
        }
    }

    // 递归方式 获取所有文件及其总大小
    public static long getAllFilesInAndSizeDir(final File file, ArrayList<File> list) {
        if (file.isFile()) {
            list.add(file);
            return file.length();
        } else {
            final File[] children = file.listFiles();
            long total = 0;
            if (children != null)
                for (final File child : children)
                    total += getAllFilesInAndSizeDir(child, list);
            return total;
        }
    }


    public static String createGalleryFile(String fileName) {
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera"
                + File.separator + fileName;
        File galleryDir = new File(filePath).getParentFile();
        if (!galleryDir.exists()) {
            try {
                galleryDir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public static String createNoneGalleryFile(String fileName) {
//        String filePath = Environment.getExternalStorageDirectory()
//                + File.separator + Environment.DIRECTORY_DCIM + File.separator;
//        if ("Xiaomi".equalsIgnoreCase(Build.BRAND)) {
//            filePath += "Camera" + File.separator;
//        }
//        filePath += fileName;
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DCIM
                + File.separator + "Camera"
                + File.separator + "." + fileName;
        File galleryDir = new File(filePath).getParentFile();
        if (!galleryDir.exists()) {
            try {
                galleryDir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public static String createDownloadFile(String fileName) {
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + Environment.DIRECTORY_DOWNLOADS
                + File.separator + fileName;
        File downloadDir = new File(filePath).getParentFile();
        if (!downloadDir.exists()) {
            try {
                downloadDir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filePath;
    }

    public static String saveToGallery(Bitmap bitmap, String fileName) {
        return saveToGallery(bitmap, fileName, 100);
    }

    public static boolean saveNoneToGallery(Bitmap bitmap, String fileName) {
        return saveNoneToGallery(bitmap, fileName, 100);
    }

    public static String saveToGallery(Bitmap bitmap, String fileName, int quality) {
        if (bitmap == null) return "";
        String filePath = createGalleryFile(fileName);

        Cache cache = getInstance();
        try {
            if (cache.initWrite(filePath, false)
                    && cache.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, quality)) {
//                try {
//                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                            filePath, fileName, null);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
                FileUtils.notifySystemToScan(filePath);
                return filePath;
            }
        } finally {
            cache.saveWrite();
        }
        return "";
    }

    public static boolean saveNoneToGallery(Bitmap bitmap, String fileName, int quality) {
        if (bitmap == null) return false;
        String filePath = createNoneGalleryFile(fileName);

        Cache cache = getInstance();
        try {
            if (cache.initWrite(filePath, false)
                    && cache.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, quality)) {
//                try {
//                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                            filePath, fileName, null);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
                FileUtils.notifySystemToScan(filePath);
                return true;
            }
        } finally {
            cache.saveWrite();
        }
        return false;
    }

    public static String saveToGallery(String filePath) {
        return saveToGallery(filePath, null);
    }

    public static String saveToGallery(String filePath, String newName) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) return "";
        String savePath = Cache.createGalleryFile(TextUtils.isEmpty(newName) ? file.getName() : newName);
        if (!FileUtils.copyFile(filePath, savePath, new FileUtils.OnReplaceListener() {
            @Override
            public boolean onReplace() {
                return false;
            }
        })) return "";
        FileUtils.notifySystemToScan(savePath);
        return savePath;
    }

    public static String getDataFromAsset(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static void tryRecycleAnimationDrawable(AnimationDrawable animationDrawable) {
        if (animationDrawable != null) {
            animationDrawable.stop();
            for (int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
                Drawable frame = animationDrawable.getFrame(i);
                if (frame instanceof BitmapDrawable) {
                    ((BitmapDrawable) frame).getBitmap().recycle();
                }
                frame.setCallback(null);
            }
            animationDrawable.setCallback(null);
        }
    }

    private boolean initReadStream() {
        if (dis == null && fis != null)
            dis = new DataInputStream(fis);
        return (dis != null);
    }

    public boolean initRead(String filePath) {
        return initRead(new File(filePath));
    }

    public boolean initRead(String pathName, String fileName) {
        return initRead(new File(pathName, fileName));
    }

    public boolean initRead(String[] cacheName, String fileName) {
        return initRead(getFile(cacheName, fileName));
    }

    public boolean initRead(File file) {
        try {
            if (hasSDCard()) {
                if (!file.exists() || file.isDirectory()) {
                    return false;
                }
                fis = new FileInputStream(file);
            } else {
                fis = context.openFileInput(FileUtils.getFileName(file));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return (fis != null);
    }

    public int readInt(int a) {
        if (!initReadStream())
            return a;
        int result = a;
        try {
            result = dis.readInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String readTXT(String txtType) {
        if (txtType == null)
            txtType = "UTF-8";

        String str = null;
        int length;
        try {
            length = fis.available();

            byte[] buffer = new byte[length];

            fis.read(buffer);

            str = EncodingUtils.getString(buffer, txtType);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return str;
    }

    public String readString() {
        if (!initReadStream())
            return null;
        String result = null;
        char c[] = new char[500], p;
        int i, j;
        try {
            i = 0;

            p = dis.readChar();

            while (p != '☆') {
                c[i++] = p;
                p = dis.readChar();
            }
            char s[] = new char[i];
            for (j = 0; j < i; j++)
                s[j] = c[j];
            result = String.valueOf(s);

        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<String> readStrings() {
        if (!initReadStream())
            return null;
        ArrayList<String> result = new ArrayList<String>();
        char c[] = new char[500], p;
        int i, j;
        try {
            while (true) {
                i = 0;

                p = dis.readChar();

                while (p != '☆') {
                    c[i++] = p;
                    p = dis.readChar();
                }
                char s[] = new char[i];
                for (j = 0; j < i; j++)
                    s[j] = c[j];
                result.add(String.valueOf(s));
            }

        } catch (EOFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void saveRead() {
        try {
            if (fis != null)
                fis.close();
            if (dis != null)
                dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean initWriteStream() {
        if (dos == null && fos != null)
            dos = new DataOutputStream(fos);
        return (dos != null);
    }

    public boolean initWrite(String filePath) {
        return initWrite(filePath, false);
    }

    public boolean initWrite(String parentPath, String fileName, boolean isSupple) {
        return initWrite(new File(parentPath, fileName).getPath(), isSupple);
    }

    public boolean initWrite(String filePath, boolean isSupple) {
        try {
            if (hasSDCard()) {
                if (!FileUtils.createFile(filePath, !isSupple)) return false;
                fos = new FileOutputStream(new File(filePath), isSupple);
            } else {
                fos = context.openFileOutput(FileUtils.getFileName(filePath),
                        isSupple ? Context.MODE_APPEND : Context.MODE_PRIVATE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return (fos != null);
    }

    public boolean writeInt(int a) {
        if (!initWriteStream())
            return false;
        try {
            dos.writeInt(a);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeString(String str) {
        if (!initWriteStream())
            return false;
        try {
            if (str != null) {
                int i;

                int len = str.length();
                for (i = 0; i < len; i++)
                    dos.writeChar(str.charAt(i));
            }
            dos.writeChar('☆');
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean writeTXT(String str, String txtType) {
        if (txtType == null)
            txtType = "UTF-8";
        byte[] bytes;
        try {
            bytes = str.getBytes(txtType);

            try {
                fos.write(bytes);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        return false;
    }

    public boolean writeBitmap(Bitmap bitmap) {
        return writeBitmap(bitmap, Bitmap.CompressFormat.PNG);
    }

    public boolean writeBitmap(Bitmap bitmap, Bitmap.CompressFormat format) {
        return writeBitmap(bitmap, format, 100);
    }

    public boolean writeBitmap(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        try {
            if (!bitmap.compress(format, quality, fos))
                return false;
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean writeWhiteBitmap(int width, int height) {
        int[] pix = new int[width * height];

        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int r = ((pix[index] >> 16) & 0xff) | 0xff;
                int g = ((pix[index] >> 8) & 0xff) | 0xff;
                int b = (pix[index] & 0xff) | 0xff;
                pix[index] = 0xff000000 | (r << 16) | (g << 8) | b;

            }

        Bitmap bitmap = Bitmap.createBitmap(pix, width, height,
                Bitmap.Config.ARGB_8888);
        return writeBitmap(bitmap);
    }

    public void saveWrite() {
        try {
            if (fos != null)
                fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (dos != null)
                dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DataOutputStream getDataOutputStream() {
        initWriteStream();
        return dos;
    }

    public DataInputStream getDataInputStream() {
        initReadStream();
        return dis;
    }
}
