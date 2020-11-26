package com.xuqiqiang.scalebox.demo.model.entity;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.xuqiqiang.scalebox.GalleryBoxAdapter;
import com.xuqiqiang.uikit.utils.ObjectUtils;

public class PhotoWrapper implements MultiItemEntity, GalleryBoxAdapter.IPhotoWrapper {
    public static final int TEXT = 1;
    public static final int IMG = 2;

    private PhotoEntity photoEntity;
    private String date;

    public PhotoWrapper(PhotoEntity photoEntity) {
        this.photoEntity = photoEntity;
    }

    public PhotoWrapper(String date) {
        this.date = date;
    }

    public PhotoEntity getPhotoEntity() {
        return photoEntity;
    }

    public void setPhotoEntity(PhotoEntity photoEntity) {
        this.photoEntity = photoEntity;
    }

    @Override
    public boolean isPhoto() {
        return photoEntity != null;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PhotoEntity)
            return ObjectUtils.equals(this.photoEntity, (PhotoEntity) obj);
        return obj instanceof PhotoWrapper && (ObjectUtils.equals(this.photoEntity, ((PhotoWrapper) obj).photoEntity));
    }

    @Override
    public int getItemType() {
        return photoEntity == null ? TEXT : IMG;
    }
}
