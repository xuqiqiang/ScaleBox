package com.snailstudio2010.imageviewer;

/**
 * Created by xuqiqiang on 2020/08/27.
 */
public interface IPhotoEntity {
    int TYPE_IMAGE = 0;
    int TYPE_VIDEO = 1;

    String getFilePath();

    int getType();
}