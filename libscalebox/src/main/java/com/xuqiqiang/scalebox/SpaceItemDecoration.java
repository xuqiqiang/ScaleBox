package com.xuqiqiang.scalebox;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by xuqiqiang on 2020/10/17.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int right;
    private int bottom;

    public SpaceItemDecoration(int right, int bottom) {
        this.right = right;
        this.bottom = bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.top = 0;
        outRect.left = 0;
        outRect.right = right;
        outRect.bottom = bottom;
    }
}