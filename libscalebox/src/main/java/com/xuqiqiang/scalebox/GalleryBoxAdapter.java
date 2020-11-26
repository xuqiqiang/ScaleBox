package com.xuqiqiang.scalebox;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.scalebox.utils.AnimUtils;
import com.xuqiqiang.scalebox.utils.Logger;
import com.xuqiqiang.scalebox.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2020/10/22.
 */
@SuppressWarnings("StatementWithEmptyBody")
public abstract class GalleryBoxAdapter<P extends GalleryBoxAdapter.IPhotoEntity, W extends GalleryBoxAdapter.IPhotoWrapper> extends ScaleBoxAdapter {
    protected List<P> mPhotoList;
    protected List<W>[] mPhotoLists;
    private List<Integer>[] mTitleLists;
    private boolean showDate;
    private boolean showPopupDate;
    //    private ViewGroup mDataContainer;
//    private SparseArray<FrameLayout> mDataContainers;
    private FrameLayout[] mDataContainers;
    private int[] mSpanCounts;

    public GalleryBoxAdapter(Context context, List<P> photoList) {
        super(context);
        mPhotoList = photoList;
//        mDataContainers = new SparseArray<>();
//        mDataContainers = new FrameLayout[];
    }

    @Override
    @CallSuper
    protected void onInitSpan(int[] spanCounts) {
        mSpanCounts = spanCounts;
        mPhotoLists = new List[spanCounts.length];
        mTitleLists = new List[spanCounts.length];
        for (int i = 0; i < spanCounts.length; i++) {
            mPhotoLists[i] = new ArrayList<>();
            mTitleLists[i] = new ArrayList<>();
        }
        mDataContainers = new FrameLayout[spanCounts.length];
    }

    public List<W> getPhotoList(int level) {
        return mPhotoLists[level];
    }

    public void setShowDate(boolean showDate) {
//        if (this.showDate != showDate) notifyDataSetChanged(true);
        this.showDate = showDate;
    }

    public void setShowPopupDate(boolean showPopupDate) {
        this.showPopupDate = showPopupDate;
        if (showPopupDate) {
//            if (mDataContainer == null) {
//                mDataContainer = new FrameLayout(mContext);
//                mPhotoBox.addView(mDataContainer, new FrameLayout.LayoutParams(
//                        ViewGroup.LayoutParams.MATCH_PARENT, Integer.MAX_VALUE));
//            }

//            rvPhotos[3].addOnScrollListener(new RecyclerView.OnScrollListener() {
//                @Override
//                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                    super.onScrollStateChanged(recyclerView, newState);
//                }
//
//                @Override
//                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                    Logger.d("onScrolled:" + dy + "," + recyclerView.computeVerticalScrollOffset());
//                    mDataContainer.setTranslationY(-recyclerView.computeVerticalScrollOffset());
//                }
//            });

            addOnScrollListener(new ScaleBoxAdapter.OnScrollListener() {
                @Override
                public void onScrollStateChanged(int level, @NonNull RecyclerView recyclerView, int newState) {
//                    if (level >= 4) {
//                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                            Fresco.getImagePipeline().resume();
//                        } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                            Fresco.getImagePipeline().pause();
//                        }
//                    }
//                    if (newState == RecyclerView.SCROLL_STATE_IDLE && level == mGalleryAdapter.getLevel()) {
//                        updateDate(level);
//                    }
                }

                @Override
                public void onScrolled(int level, @NonNull RecyclerView recyclerView, int dx, int dy) {
//                    Logger.d("onScrolled:" + recyclerView.computeVerticalScrollOffset() + ","
//                    +recyclerView.computeVerticalScrollRange() + ","
//                    + recyclerView.computeVerticalScrollExtent());
                    FrameLayout dataContainer = mDataContainers[level];
                    if (dataContainer != null)
                        dataContainer.setTranslationY(-recyclerView.computeVerticalScrollOffset());
                }
            });
        }
    }

    public void setDate(List<P> photoList) {
        mPhotoList.clear();
        if (photoList != null) mPhotoList.addAll(photoList);
        notifyDataSetChanged(true);
    }

    @Override
    public int toPreviewIndex(int level, int position) {
        int titleIndex = 0;
        for (; titleIndex < mTitleLists[level].size()
                && mTitleLists[level].get(titleIndex) - 1 <= position; titleIndex += 1)
            ;
        return position + titleIndex;
    }

    @Override
    public int toRealIndex(int level, int index) {
        int titleIndex = 0;
        for (; titleIndex < mTitleLists[level].size() &&
                mTitleLists[level].get(titleIndex) + titleIndex <= index; titleIndex += 1)
            ;
        return index - titleIndex;
    }

    @Override
    protected void refreshData(int index) {
        mPhotoLists[index].clear();
        mTitleLists[index].clear();
        String curDate = "";
        int lastDataIndex = -1;
        Logger.d("refreshData index: " + index + ", " + (getCellSize(index) + getCellGap(index)));
        int cellSize = (int) Math.ceil(getCellSize(index) + getCellGap(index));
        for (int i = 0; i < mPhotoList.size(); i++) {
            P entity = mPhotoList.get(i);
            if (showDate) {
                String date = entity.getDate(index);
                if (!TextUtils.isEmpty(date) && !TextUtils.equals(date, curDate)) {
                    curDate = date;
                    mPhotoLists[index].add(toPhotoWrapper(date));
                    mTitleLists[index].add(i + 1);
                }
            } else if (showPopupDate) {
                if (i % mSpanCounts[index] == 0) {
                    String date = entity.getDate(index);
                    Logger.d("textView 0:" + date);
                    if (!TextUtils.isEmpty(date) && !TextUtils.equals(date, curDate)) {
                        curDate = date;
//                    mPhotoLists[index].add(toPhotoWrapper(date));
//                    mTitleLists[index].add(i + 1);

                        int dataIndex = i / mSpanCounts[index];
                        Logger.d("textView 1:" + i + ", " + dataIndex);
                        if (dataIndex > lastDataIndex) {
                            lastDataIndex = dataIndex;
                            TextView textView = new TextView(mContext);
                            textView.setText(date);
                            textView.setTextColor(Color.BLACK);
                            textView.setTextSize(15);
                            textView.setBackgroundColor(Color.WHITE);
//                    textView.setGravity(Gravity.CENTER);
                            textView.setPadding((int) Utils.dip2px(mContext, 12),
                                    (int) Utils.dip2px(mContext, 4),
                                    (int) Utils.dip2px(mContext, 12),
                                    (int) Utils.dip2px(mContext, 4));
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            lp.setMargins((int) Utils.dip2px(mContext, 2),
                                    (int) (dataIndex * cellSize + Utils.dip2px(mContext, 2)), 0, 0);

                            FrameLayout dataContainer = mDataContainers[index];
                            if (dataContainer == null) {
                                dataContainer = new FrameLayout(mContext);
                                dataContainer.setVisibility(View.INVISIBLE);
                                mDataContainers[index] = dataContainer;
                                mPhotoBox.addView(dataContainer, new FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT, Integer.MAX_VALUE));
                            }
                            dataContainer.addView(textView, lp);
                            Logger.d("textView 2:" + (int) (dataIndex * cellSize) + "," + dataIndex + "," + cellSize);
                        }
                    }
                }
            }
            mPhotoLists[index].add(toPhotoWrapper(entity));
        }
    }

    @Override
    protected void onScaleStart() {
        if (mDataContainers != null) {
            for (FrameLayout dataContainer : mDataContainers) {
                if (dataContainer != null) AnimUtils.hideView(dataContainer);
//                    dataContainer.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onScaleEnd() {
        FrameLayout dataContainer = mDataContainers[getLevel()];
        if (dataContainer != null) AnimUtils.showView(dataContainer);
//            dataContainer.setVisibility(View.VISIBLE);
    }


    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected final RecyclerView.Adapter initPhotoAdapter(int level, int spanSize) {
        return initPhotoAdapter(mPhotoLists[level], spanSize);
    }

    @SuppressWarnings("rawtypes")
    protected abstract RecyclerView.Adapter initPhotoAdapter(List<W> list, int spanSize);

    @Override
    protected int getNextAvailablePhoto(int level, int index) {
        W wrapper = mPhotoLists[level].get(index);
        if (!wrapper.isPhoto()) {
            for (index += 1; index < mPhotoLists[level].size()
                    && !mPhotoLists[level].get(index).isPhoto(); index += 1)
                ;
            if (index >= mPhotoLists[level].size()) return -1;
        }
        return index;
    }

    protected abstract W toPhotoWrapper(P entity);

    protected abstract W toPhotoWrapper(String date);

    public interface IPhotoEntity {
        String getDate(int level);
    }

    public interface IPhotoWrapper {
        IPhotoEntity getPhotoEntity();

        boolean isPhoto();
    }
}
