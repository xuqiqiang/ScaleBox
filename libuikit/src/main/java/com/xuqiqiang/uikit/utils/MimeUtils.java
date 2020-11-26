package com.xuqiqiang.uikit.utils;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by xuqiqiang on 2019/11/13.
 */
public class MimeUtils {

    private static final String[][] MIME_IMAGE = {
            {".png", "image/png"},
            {".bmp", "image/bmp"},
            {".gif", "image/gif"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
    };

    private static final String[][] MIME_VIDEO = {
            {".mp4", "video/mp4"},
            {".3gp", "video/3gpp"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
    };

    private static final String[][] MIME_AUDIO = {
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".mpga", "audio/mpeg"},
            {".ogg", "audio/ogg"},
    };

    private static final String[][] MIME_TEXT = {
            {".c", "text/plain"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".java", "text/plain"},
            {".log", "text/plain"},
            //{".xml",    "text/xml"},
            {".xml", "text/plain"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".sh", "text/plain"},
            {".txt", "text/plain"},
    };

    private static final String[][] MIME_APP = {
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gtar", "application/x-gtar"},
            {".class", "application/octet-stream"},
            {".apk", "application/vnd.android.package-archive"},
            {".bin", "application/octet-stream"},
            {".gz", "application/x-gzip"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".msg", "application/vnd.ms-outlook"},
            {".js", "application/x-javascript"},
            {".jar", "application/java-archive"},
            {".pdf", "application/pdf"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".rar", "application/x-rar-compressed"},
            {".rtf", "application/rtf"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".wps", "application/vnd.ms-works"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
    };

    private static final String[][] MIME_MapTable = {
            //{后缀名，    MIME类型}
            {".png", "image/png"},
            {".bmp", "image/bmp"},
            {".gif", "image/gif"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},

            {".mp4", "video/mp4"},
            {".3gp", "video/3gpp"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},

            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".mpga", "audio/mpeg"},
            {".ogg", "audio/ogg"},

            {".c", "text/plain"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".java", "text/plain"},
            {".log", "text/plain"},
            //{".xml",    "text/xml"},
            {".xml", "text/plain"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".sh", "text/plain"},
            {".txt", "text/plain"},

            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gtar", "application/x-gtar"},
            {".class", "application/octet-stream"},
            {".apk", "application/vnd.android.package-archive"},
            {".bin", "application/octet-stream"},
            {".gz", "application/x-gzip"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".msg", "application/vnd.ms-outlook"},
            {".js", "application/x-javascript"},
            {".jar", "application/java-archive"},
            {".pdf", "application/pdf"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".rar", "application/x-rar-compressed"},
            {".rtf", "application/rtf"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".wps", "application/vnd.ms-works"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},

            {"", "*/*"}
    };

    public static String getMIMEType(String suffix) {
        return getMIMEType(suffix, MIME_MapTable);
    }

    public static String getMIMEType(String suffix, String[][] mapTable) {

        String type = "*/*";
        if (TextUtils.isEmpty(suffix))
            return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < mapTable.length; i++) {
            if (suffix.equalsIgnoreCase(mapTable[i][0]))
                type = mapTable[i][1];
        }
        return type;
    }

    public static String getMIMEType(File file) {
        String fName = file.getName();
        return getUrlMIMEType(fName);
    }

    public static String getUrlMIMEType(String url) {
        return getMIMEType(StringUtils.getSuffix(url).toLowerCase());
    }

    public static String getUrlMIMEType(String url, String[][] mapTable) {
        return getMIMEType(StringUtils.getSuffix(url).toLowerCase(), mapTable);
    }

    public static boolean isVideo(String url) {
        String type = getUrlMIMEType(url, MIME_VIDEO);
        return type.contains("video");
    }

    public static boolean isAudio(String url) {
        String type = getUrlMIMEType(url, MIME_AUDIO);
        return type.contains("audio");
    }

    public static boolean isImage(String url) {
        String type = getUrlMIMEType(url, MIME_IMAGE);
        return type.contains("image");
    }

    public static boolean isText(String url) {
        String type = getUrlMIMEType(url, MIME_TEXT);
        return type.contains("text");
    }

    public static boolean isApp(String url) {
        String type = getUrlMIMEType(url, MIME_APP);
        return type.contains("application");
    }
}