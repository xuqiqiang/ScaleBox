package com.xuqiqiang.scalebox.demo.controller;

import android.app.Activity;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.scalebox.demo.model.entity.Album;
import com.xuqiqiang.scalebox.demo.model.loader.AlbumLoader;
import com.xuqiqiang.scalebox.demo.view.adapter.AlbumAdapter;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public class AlbumController extends BaseLoaderController
        implements AlbumAdapter.OnItemClickListener {

    private AlbumAdapter albumAdapter;
    private OnDirectorySelectListener directorySelectListener;

    public void onCreate(Activity activity, RecyclerView recyclerView,
                         OnDirectorySelectListener directorySelectListener) {
        super.onCreate(activity);
        this.albumAdapter = new AlbumAdapter(activity, null, 0);
        this.directorySelectListener = directorySelectListener;
        recyclerView.setAdapter(albumAdapter);
        albumAdapter.setOnItemClickListener(this);
    }

    @Override
    protected int getLoaderId() {
        return ALBUM_LOADER_ID;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return AlbumLoader.newInstance(context);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        albumAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        albumAdapter.swapCursor(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void loadAlbums() {
        loaderManager.initLoader(getLoaderId(), null, this);
    }

    @Override
    public void onItemClick(Album album, int position) {
        if (directorySelectListener != null)
            directorySelectListener.onSelect(album);
    }

    public interface OnDirectorySelectListener {
        void onSelect(Album album);

        void onReset(Album album);
    }
}
