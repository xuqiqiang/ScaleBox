package com.xuqiqiang.scalebox.demo.model.loader;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.xuqiqiang.scalebox.demo.model.entity.Album;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public class AlbumLoader extends CursorLoader {

    private static final String[] PROJECTION = {MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media._ID,
            "count(bucket_id) as cou"};
    private static final String BUCKET_GROUP_BY = ") GROUP BY  1,(2";
    private static final String BUCKET_ORDER_BY = "count(bucket_id) DESC";// "MAX(datetaken) DESC";
    private static final String MEDIA_ID_DUMMY = String.valueOf(-1);
    private static final String IS_LARGE_SIZE = " _size > ? or _size is null";

    private AlbumLoader(Context context, Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static CursorLoader newInstance(Context context) {
        return newInstance(context, 0);
    }

    public static CursorLoader newInstance(Context context, long minByte) {
        return new AlbumLoader(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION,
                IS_LARGE_SIZE + BUCKET_GROUP_BY, new String[]{minByte + ""},
                BUCKET_ORDER_BY);
    }

    @Override
    public Cursor loadInBackground() {
        long count = 0;
        Cursor albums = null;
        try {
            albums = super.loadInBackground();
            if (albums.getCount() > 0) {
                while (albums.moveToNext()) {
                    count += albums.getLong(3);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        MatrixCursor allAlbum = new MatrixCursor(PROJECTION);
        allAlbum.addRow(
                new String[]{
                        Album.ALBUM_ID_ALL,
                        getContext().getString(Album.ALBUM_NAME_ALL_RES_ID),
                        MEDIA_ID_DUMMY,
                        count + ""});
        return new MergeCursor(new Cursor[]{allAlbum, albums});
    }
}
