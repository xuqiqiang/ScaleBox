package com.xuqiqiang.scalebox.demo.view.adapter;

import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xuqiqiang.scalebox.ScaleBox;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoWrapper;
import com.xuqiqiang.scalebox.demo.utils.FrescoImageLoader;
import com.xuqiqiang.uikit.utils.ScreenUtils;
import com.xuqiqiang.scalebox.utils.Logger;

import java.io.File;
import java.util.List;

public class PhotoAdapter extends BaseMultiItemQuickAdapter<PhotoWrapper, BaseViewHolder> implements ScaleBox.IGalleryAdapter {

    private int mOffsetCount;
    private int mSpanSize;
    private boolean hasAttach;

    public PhotoAdapter(final List<PhotoWrapper> data, final int spanSize) {
        super(data);
        mSpanSize = spanSize;
        addItemType(PhotoWrapper.TEXT, R.layout.photo_list_item_date);
        addItemType(PhotoWrapper.IMG, R.layout.photo_list_item);
        if (spanSize >= 9)
            addItemType(PhotoWrapper.TEXT, R.layout.photo_list_item_date_month);
    }

    @Override
    public void notifyMockItemInserted(int itemCount) {

        if (!hasAttach) {
            hasAttach = true;
            getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) { //当前状态为停止滑动
                        if (!getRecyclerView().canScrollVertically(-1)) { // 到达顶部
                            Logger.d("_test_s_ 到达顶部");
                            if (mOffsetCount > 0) {
                                List<PhotoWrapper> data = getData();
                                for (int i = 0; i < mOffsetCount; i++)
                                    data.remove(0);
                                notifyItemRangeRemoved(0, mOffsetCount);
                                mOffsetCount = 0;
                            }
                        }
                    }
                }
            });
        }

        Logger.d("_test_0_ notifyMockItemInserted:" + itemCount + "," + mSpanSize);
        if (mOffsetCount == itemCount) return;

        List<PhotoWrapper> data = getData();
        PhotoEntity photoEntity = new PhotoEntity(null);
        if (mOffsetCount < itemCount) {
            for (int i = mOffsetCount; i < itemCount; i++)
                data.add(0, new PhotoWrapper(photoEntity));
        } else {
            for (int i = mOffsetCount; i > itemCount; i--)
                data.remove(0);
        }

        mOffsetCount = itemCount;
        notifyDataSetChanged();
    }

    @Override
    public int getOffsetCount() {
        return mOffsetCount;
    }

    @Override
    public void resetOffsetCount() {
        mOffsetCount = 0;
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, PhotoWrapper item) {
        switch (helper.getItemViewType()) {
            case PhotoWrapper.TEXT:
                helper.setText(R.id.tv_date, item.getDate());
                break;
            case PhotoWrapper.IMG:
                GridLayoutManager lm = (GridLayoutManager) getRecyclerView().getLayoutManager();
                int size = (int) (ScreenUtils.getWidth() / (float) lm.getSpanCount());
                PhotoEntity photoEntity = item.getPhotoEntity();
                if (TextUtils.isEmpty(photoEntity.getFilePath())) {
                    ((ImageView) helper.getView(R.id.iv_photo)).setImageDrawable(new ColorDrawable(0xFF1B1E1E));
//                    FrescoImageLoader.getInstance().bindImage((ImageView) helper.getView(R.id.iv_photo),
//                            Uri.parse(""), size, size);
                    return;
                }
                FrescoImageLoader.getInstance().bindImage((ImageView) helper.getView(R.id.iv_photo),
                        Uri.fromFile(new File(photoEntity.getFilePath())), size, size);
                break;
            default:
                break;
        }
    }

}
