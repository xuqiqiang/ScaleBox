package com.xuqiqiang.scalebox.demo.model.entity;

import android.text.TextUtils;

import com.xuqiqiang.scalebox.GalleryBoxAdapter;
import com.xuqiqiang.uikit.utils.TimeUtils;
import com.xuqiqiang.uikit.utils.MimeUtils;
import com.xuqiqiang.uikit.utils.ObjectUtils;

import java.io.File;
import java.io.Serializable;

/**
 * Created by xuqiqiang on 2020/08/27.
 */
public class PhotoEntity implements Serializable, GalleryBoxAdapter.IPhotoEntity {

    public static final int TYPE_IMAGE = 0, TYPE_VIDEO = 1;
    private static final long serialVersionUID = 1L;
    private String filePath;
    private long createTime;

    public PhotoEntity(String filePath) {
        this.filePath = filePath;
        if (!TextUtils.isEmpty(filePath))
            this.createTime = new File(filePath).lastModified();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getType() {
        int type = TYPE_IMAGE;
        String mimeType = MimeUtils.getUrlMIMEType(filePath);
        if (mimeType.contains("video")) {
            type = TYPE_VIDEO;
        }
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhotoWrapper)
            return ObjectUtils.equals(this, ((PhotoWrapper) obj).getPhotoEntity());
        return obj instanceof PhotoEntity && (TextUtils.equals(this.filePath, ((PhotoEntity) obj).filePath));
    }

    @Override
    public String toString() {
        return filePath;
    }

    @Override
    public String getDate(int level) {
        if (level == 2) {
            return TimeUtils.formatTime(createTime, "MM月dd日");
        } else if (level == 3) {
            return TimeUtils.formatTime(createTime, "yyyy年MM月");
        }
//
//        if (level == 3) {
//            return TimeUtils.formatTime(createTime, "yyyy年MM月");
//        }
        return null;
    }
}