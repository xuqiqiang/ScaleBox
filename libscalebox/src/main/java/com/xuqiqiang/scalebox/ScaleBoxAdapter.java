package com.xuqiqiang.scalebox;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.scalebox.utils.Logger;
import com.xuqiqiang.scalebox.utils.Utils;

/**
 * Created by xuqiqiang on 2020/10/22.
 */
public abstract class ScaleBoxAdapter {
    private static final int ANIM_DURING = 300;
    private static final float TOUCH_GAP = 100;
    private static final int mDefaultLevel = 2;
    protected final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    protected RecyclerView[] rvPhotos;
    protected ScaleBox mPhotoBox;
    private int[] mSpanCounts;
    private int level = mDefaultLevel;
    private int mRVBackgroundColor;
    private int mBaseSpan;
    private OnScaleListener mOnScaleListener;

    public ScaleBoxAdapter(Context context) {
        this.mContext = context;
    }

    protected void onInitSpan(int[] spanCounts) {
    }

    final void init(ScaleBox photoBox) {
        this.mPhotoBox = photoBox;
//        mPhotoBox.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mSpanCounts = initSpanCounts();
        onInitSpan(mSpanCounts);
        int baseSpan = 0;
        for (int i = 0; i < mSpanCounts.length; i += 1) {
            if (mSpanCounts[i] > 1) {
                baseSpan = i;
                break;
            }
        }
        mBaseSpan = baseSpan;
        rvPhotos = new RecyclerView[mSpanCounts.length];
        if (rvPhotos.length == 1) level = 1;
        for (int i = 0; i < mSpanCounts.length; i++) {
            rvPhotos[i] = new RecyclerView(mContext);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            int gap = (int) getCellGap(i);
            if (gap > 0) {
                lp.setMargins(0, 0, -gap, 0);
            }
            int vGap = gap;
            if (vGap <= 0)
                vGap = (int) (getCellGap() * mSpanCounts[mBaseSpan] / (float) mSpanCounts[i]);
            photoBox.addView(rvPhotos[i], 0, lp);
            if (mRVBackgroundColor != 0)
                rvPhotos[i].setBackgroundColor(mRVBackgroundColor);

            if (i != level - 1 && rvPhotos.length >= level) {
                rvPhotos[i].setAlpha(0);
                rvPhotos[i].setVisibility(View.INVISIBLE);
            }
            rvPhotos[i].setLayoutManager(new GridLayoutManager(mContext, mSpanCounts[i],
                    GridLayoutManager.VERTICAL, false));
            rvPhotos[i].setAdapter(initPhotoAdapter(i, mSpanCounts[i]));
            rvPhotos[i].addItemDecoration(new SpaceItemDecoration(gap, vGap));
//            rvPhotos[i].setPivotX(ScreenUtils.getWidth() / 2f);
            rvPhotos[i].setPivotX(mPhotoBox.getWidth() / 2f);
            rvPhotos[i].setPivotY(0);

            if (i >= level) {
                rvPhotos[i].setScaleX(mSpanCounts[i] / (float) mSpanCounts[i - 1]);
                rvPhotos[i].setScaleY(mSpanCounts[i] / (float) mSpanCounts[i - 1]);
            }
        }
        initTouch(null);
    }

    protected float getCellGap() {
        return Utils.dip2px(mContext, 4);
    }

    protected float getCellGap(int level) {
        if (level < mBaseSpan) return 0;
        else {
            float gap = getCellGap();
            if (level > mBaseSpan) gap *= mSpanCounts[mBaseSpan] / (float) mSpanCounts[level];
            return gap;
        }
    }

    protected float getTouchGap() {
        return Utils.dip2px(mContext, TOUCH_GAP);
    }

    protected int[] initSpanCounts() {
        return new int[]{1, 3, 5, 9};
    }

    void initTouch(NestedScrollView svContainer) {
        mPhotoBox.setOnScaleListener(new ScaleListener(svContainer));
    }

    public void addOnScrollListener(@NonNull final OnScrollListener listener) {
        for (int i = 0; i < mSpanCounts.length; i++) {
            final int l = i;
            rvPhotos[i].addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    listener.onScrollStateChanged(l, recyclerView, newState);
//                    onRecyclerViewScrollStateChanged(l, recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    listener.onScrolled(l, recyclerView, dx, dy);
                }
            });
        }
    }

    public void addOnScrollListener(@NonNull RecyclerView.OnScrollListener listener) {
        for (RecyclerView rvPhoto : rvPhotos) {
            rvPhoto.addOnScrollListener(listener);
        }
    }

    public void setOnScaleListener(OnScaleListener listener) {
        this.mOnScaleListener = listener;
    }

    public RecyclerView getRecyclerView(int level) {
        return rvPhotos[level];
    }

    /**
     * 需要设置背景颜色，不然缩放时可能会出现闪烁
     *
     * @param color 背景颜色
     */
    public void setRVBackgroundColor(@ColorInt int color) {
        mRVBackgroundColor = color;
        if (rvPhotos != null) {
            for (RecyclerView rvPhoto : rvPhotos) {
                rvPhoto.setBackgroundColor(color);
            }
        }
    }

    public int getLevel() {
        return level - 1;
    }

    public void setLevel(int level) {
        if (mPhotoBox != null) throw new IllegalStateException("只能在调用setAdapter之前设置level");
        this.level = level + 1;
    }

    public View getPhotoPreviewView(int position) {
        return getPhotoPreviewView(level - 1, position);
    }

    public View getPhotoPreviewView(int level, int position) {
        return rvPhotos[level].getLayoutManager().findViewByPosition(toPreviewIndex(level, position));
    }

    public abstract int toPreviewIndex(int level, int position);

    public abstract int toRealIndex(int level, int position);

    public boolean avoidEdge() {
        return true;
    }

//    public View getPhotoPreviewPhoto(int position) {
//        return ((BaseQuickAdapter) rvPhotos[level - 1].getAdapter()).getViewByPosition(
//                toPreviewIndex(level - 1, position), R.id.iv_photo);
//    }

    public void notifyDataSetChanged() {
        notifyDataSetChanged(false);
    }

    public void notifyDataSetChanged(boolean forceSync) {
        for (int index = 0; index < rvPhotos.length; index++) {
            RecyclerView.Adapter adapter = rvPhotos[index].getAdapter();
            if (forceSync) {
                refreshData(index);
                if (adapter instanceof ScaleBox.IGalleryAdapter) {
                    ((ScaleBox.IGalleryAdapter) adapter).resetOffsetCount();
                }
            }
            rvPhotos[index].scrollToPosition(0);
            rvPhotos[index].setVisibility(View.VISIBLE);
            if (adapter != null)
                adapter.notifyDataSetChanged();
            final int i = index;
            Logger.d("_test_1_ 00 :" + i);
            rvPhotos[index].post(new Runnable() {
                @Override
                public void run() {
                    Logger.d("_test_1_ 0 :" + i);
                    if (i != level - 1 && rvPhotos.length >= level) {
                        rvPhotos[i].setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
        if (mPhotoBox.mOnScaleListener != null) {
            rvPhotos[level - 1].post(new Runnable() {
                @Override
                public void run() {
                    Logger.d("_test_1_ onScaleStart 1");
                    mPhotoBox.mOnScaleListener.onScaleStart(300, 500, new Runnable() {
                        @Override
                        public void run() {
                            Logger.d("_test_1_ onScaleStart 2");
                            for (int i = 0; i < rvPhotos.length; i++) {
                                if (i != level - 1 && rvPhotos.length >= level) {
                                    rvPhotos[i].setVisibility(View.INVISIBLE);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    protected abstract void refreshData(int level);

    public void notifyItemChanged(int position) {
        for (int i = 0; i < rvPhotos.length; i++) {
            rvPhotos[i].getAdapter().notifyItemChanged(toPreviewIndex(i, position));
        }
    }

    protected abstract RecyclerView.Adapter<RecyclerView.ViewHolder> initPhotoAdapter(int level, int spanSize);

    protected int getNextAvailablePhoto(int level, int index) {
        return index;
    }

    protected void loadImage(RecyclerView.ViewHolder viewHolder, int level, int realPosition) {
    }

    /**
     * level_dst的cell相对于level_src的cell的放大倍数
     */
    private float getScaleRatio(int src, int dst) {
        if (getCellGap(src) == 0 || getCellGap(dst) == 0) {
            int gWidth = mPhotoBox.getWidth();
            if (gWidth > 0) {
                int width1 = Math.round((gWidth + getCellGap(src)) / (float) mSpanCounts[src] - getCellGap(src));
                int width2 = Math.round((gWidth + getCellGap(dst)) / (float) mSpanCounts[dst] - getCellGap(dst));
                return width2 / (float) width1;
            }
        }
        return mSpanCounts[src] / (float) mSpanCounts[dst];
    }

    public float getCellSize(int level) {
        int gWidth = mPhotoBox.getWidth();
        return (gWidth + getCellGap(level)) / (float) mSpanCounts[level] - getCellGap(level);
    }

    protected void onScaleStart() {
    }

    protected void onScaleEnd() {
    }

    public interface OnScrollListener {
        void onScrollStateChanged(int level, @NonNull RecyclerView recyclerView, int newState);

        void onScrolled(int level, @NonNull RecyclerView recyclerView, int dx, int dy);
    }

    public interface OnScaleListener {
        void onScaleChanged(int level);
    }

    private class ScaleListener implements ScaleBox.OnScaleListener {

        private NestedScrollView svContainer;

        private float startRadio;
        private float startRawY;
        private float fromScrollY;
        private float toScrollY;

        private int lastComputePosition = -1;
        private float lastComputePercent;

        public ScaleListener(NestedScrollView svContainer) {
            this.svContainer = svContainer;
        }

        @Override
        public void onScale(float radio, float offsetX, float offsetY) {
            int maxScale = rvPhotos.length - mDefaultLevel;
            int minScale = 1 - mDefaultLevel;
            radio = Math.max(Math.min(radio - (mDefaultLevel - level), maxScale + 0.2f), minScale - 0.5f);

            for (int i = 0; i < rvPhotos.length - 1; i++) {
                if (radio > i + minScale + 1) {
                    if (rvPhotos[i].getAlpha() > 0)
                        rvPhotos[i].setAlpha(0);
                }

                if (avoidEdge()) {
                    if (radio < maxScale - i) {
                        if (rvPhotos[rvPhotos.length - 1 - i].getAlpha() < 1)
                            rvPhotos[rvPhotos.length - 1 - i].setAlpha(1);
                    }
                } else {
                    if (radio < maxScale - 1 - i) {
                        if (rvPhotos[rvPhotos.length - 1 - i].getAlpha() > 0)
                            rvPhotos[rvPhotos.length - 1 - i].setAlpha(0);
                    }
                }
            }

            int offset = 0;
            float r = radio;

            if (mDefaultLevel > rvPhotos.length - 1) {
                offset = mDefaultLevel - (rvPhotos.length - 1);
                r = radio + offset;
            } else {
                while (r < 0 || r > 1) {
                    if (r < 0) {
                        if (mDefaultLevel - offset - 1 < 1 || mDefaultLevel - offset - 1 >= rvPhotos.length)
                            break;
                        offset += 1;
                    } else {
                        if (mDefaultLevel - offset + 1 < 1 || mDefaultLevel - offset + 1 >= rvPhotos.length)
                            break;
                        offset -= 1;
                    }
                    r = radio + offset;
                }
            }

            setScale(r, mDefaultLevel - offset);
        }

        private void setScale(float radio, int level) {
            if (level > 0) {
                rvPhotos[level - 1].setAlpha(Math.max(Math.min(1 - radio, 1f), 0f));

                float scaleRatio = getScaleRatio(level - 1, level);
                rvPhotos[level - 1].setScaleX(1 - radio + scaleRatio * radio);
                rvPhotos[level - 1].setScaleY(1 - radio + scaleRatio * radio);
                rvPhotos[level].setScaleX((1 - radio) / scaleRatio + radio);
                rvPhotos[level].setScaleY((1 - radio) / scaleRatio + radio);

//                rvPhotos[level - 1].setScaleX(1 - radio + mSpanCounts[level - 1] / (float) mSpanCounts[level] * radio);
//                rvPhotos[level - 1].setScaleY(1 - radio + mSpanCounts[level - 1] / (float) mSpanCounts[level] * radio);
//                rvPhotos[level].setScaleX(mSpanCounts[level] / (float) mSpanCounts[level - 1] * (1 - radio) + radio);
//                rvPhotos[level].setScaleY(mSpanCounts[level] / (float) mSpanCounts[level - 1] * (1 - radio) + radio);

                if (avoidEdge()) {
                    for (int i = level + 1; i < rvPhotos.length; i += 1) {
                        float r1 = getScaleRatio(i, level - 1);
                        float r2 = getScaleRatio(i, level);
                        float scale = r1 * (1 - radio) + radio * r2;
                        rvPhotos[i].setScaleX(scale);
                        rvPhotos[i].setScaleY(scale);
//                        rvPhotos[i].setScaleX(mSpanCounts[i] / (float) mSpanCounts[level - 1] * (1 - radio) + radio * (mSpanCounts[i] / (float) mSpanCounts[level]));
//                        rvPhotos[i].setScaleY(mSpanCounts[i] / (float) mSpanCounts[level - 1] * (1 - radio) + radio * (mSpanCounts[i] / (float) mSpanCounts[level]));
                    }
                } else {
                    rvPhotos[level].setAlpha(Math.max(Math.min(radio, 1f), 0f));
                }

            } else if (level == 0) {
                rvPhotos[level].setScaleX(2 * (1 - radio) + radio);
                rvPhotos[level].setScaleY(2 * (1 - radio) + radio);
            }

            if (svContainer != null) {
                fromScrollY = startRadio * rvPhotos[level - 1].getHeight() + mPhotoBox.getTop() - startRawY;
                toScrollY = startRadio * rvPhotos[level].getHeight() + mPhotoBox.getTop() - startRawY;

                Logger.d("setUi5 startRadio:" + startRadio);
                Logger.d("setUi5 fromScrollY:" + fromScrollY);
                Logger.d("setUi5 toScrollY:" + toScrollY);

                int scrollY = (int) (toScrollY * radio + fromScrollY * (1 - radio));
                svContainer.scrollTo(0, scrollY);
            }
        }

        @Override
        public void onScaleStart(float touchX, float touchY, Runnable event) {
            try {
                if (level < 1) return;
                RecyclerView view = rvPhotos[level - 1];

                Logger.d("onScale touchY:" + touchY + ",getHeight:" + view.getHeight()
                        + ",computeVerticalScrollRange:" + view.computeVerticalScrollRange()
                        + ",computeVerticalScrollExtent:" + view.computeVerticalScrollExtent()
                        + ",computeVerticalScrollOffset:" + view.computeVerticalScrollOffset());

                if (svContainer == null) {

                    // 获取当前选中的view
                    View child = view.findChildViewUnder(touchX, touchY);
                    if (child != null) {
                        int touchIndex = view.getChildLayoutPosition(child);
                        int index = touchIndex - ((ScaleBox.IGalleryAdapter) view.getAdapter()).getOffsetCount();
                        Logger.d("onScale getChildLayoutPosition:" + index);
                        int id = getNextAvailablePhoto(level - 1, index);
                        if (id != index) {
                            if (id < 0 || id >= view.getAdapter().getItemCount()) return;
                            View itemView = view.getLayoutManager().findViewByPosition(id);
                            if (itemView != null) child = itemView;
                            index = id;
                            touchIndex = index + ((ScaleBox.IGalleryAdapter) view.getAdapter()).getOffsetCount();
                        }
                        // 获取当前选中的view的真实index
                        final int realPosition = toRealIndex(level - 1, index);
                        loadImage(view.getChildViewHolder(child), level - 1, realPosition);
                        Logger.d("onScale getChildLayoutPosition realPosition:" + realPosition);

                        final float pivotX = child.getLeft() + child.getTranslationX() + child.getWidth() / 2f;
                        final float pivotY = child.getTop() + child.getTranslationY() + child.getHeight() / 2f;

                        float percent;
                        if (realPosition == lastComputePosition) {
                            percent = lastComputePercent;
                        } else {
                            if (mSpanCounts[level - 1] == 1) percent = 0.5f;
                            else {
                                percent = (touchIndex % mSpanCounts[level - 1]) / (float) (mSpanCounts[level - 1] - 1);
                            }
                            lastComputePosition = realPosition;
                            lastComputePercent = percent;
                        }


                        for (int i = 0; i < rvPhotos.length; i += 1) {
                            final RecyclerView v = rvPhotos[i];
                            v.setPivotX(pivotX);
                            v.setPivotY(pivotY);
                            if (i != level - 1) {
                                final GridLayoutManager layoutManager = (GridLayoutManager) v.getLayoutManager();
                                int firstPosition = layoutManager.findFirstVisibleItemPosition();
                                int lastPosition = layoutManager.findLastVisibleItemPosition();
                                Logger.d("onScale getChildLayoutPosition firstPosition:" + firstPosition + "," + lastPosition);
                                int p = toPreviewIndex(i, realPosition);// + ((ScaleBox.IGalleryAdapter) v.getAdapter()).getOffsetCount();

                                int offsetCount = 0;

                                int targetP = (int) (percent * (mSpanCounts[i] - 1));
                                if (mSpanCounts[i] == 3) {
                                    if (percent >= 0.25f && percent <= 0.75f) targetP = 1;
                                }

                                int displayPosition = p % mSpanCounts[i];

//                                Logger.d("_test_0_ percent:" + percent + ", targetP:" + targetP + ", displayPosition:" + displayPosition);
//                                    if (displayPosition != mSpanCounts[i] / 2) {
                                offsetCount = targetP - displayPosition;
                                if (offsetCount < 0) offsetCount += mSpanCounts[i];
                                ((ScaleBox.IGalleryAdapter) v.getAdapter()).notifyMockItemInserted(
                                        offsetCount);
                                p += offsetCount;

//                                if (index % 3 == 1) {// && i == 2) {
//                                    int displayPosition = p % mSpanCounts[i];
////                                    if (displayPosition != mSpanCounts[i] / 2) {
//                                        offsetCount = (mSpanCounts[i] / 2) - displayPosition;
//                                        if (offsetCount < 0) offsetCount += mSpanCounts[i];
//                                        ((ScaleBox.IGalleryAdapter) v.getAdapter()).notifyMockItemInserted(
//                                                offsetCount);
//                                        p += offsetCount;
////                                    }
//                                }


                                final int position = p;
                                final int rvIndex = i;

                                Logger.d("_test_1_ 1 rvIndex:" + rvIndex + ", " + offsetCount);

                                Runnable r = new Runnable() {
                                    @Override
                                    public void run() {
                                        Logger.d("_test_1_ 2 rvIndex:" + rvIndex);
                                        View itemView = layoutManager.findViewByPosition(position);
                                        Logger.d("onScale getChildLayoutPosition2:"
                                                + (itemView.getTop() + itemView.getTranslationY()) + "," + itemView.getHeight());

                                        float itemX = itemView.getLeft() + itemView.getTranslationX() + itemView.getWidth() / 2f;
                                        float itemY = itemView.getTop() + itemView.getTranslationY() + itemView.getHeight() / 2f;
                                        v.setPivotX(itemX);

                                        float scrollY = itemY - pivotY;
                                        int scrollRange = v.computeVerticalScrollRange();
                                        int scrollBottom = v.computeVerticalScrollOffset() + v.computeVerticalScrollExtent();
                                        if (scrollY + scrollBottom > scrollRange) {
                                            v.setTranslationY(scrollRange - (scrollY + scrollBottom));
                                            v.setPivotY(itemY);
                                        } else {
                                            float offsetY = scrollY + v.computeVerticalScrollOffset();
                                            Logger.d("onScale getChildLayoutPosition offsetY:" + offsetY);
                                            if (offsetY < 0) {
                                                v.setTranslationY(-offsetY);
                                                v.setPivotY(itemY);
                                            } else {
                                                v.setTranslationY(0);
                                            }
                                        }
                                        v.scrollBy(0, (int) scrollY);
                                        v.setTranslationX(pivotX - itemX);
                                        loadImage(v.findViewHolderForAdapterPosition(position), rvIndex, realPosition);
                                    }
                                };
                                if (position < firstPosition || position > lastPosition || offsetCount > 0) {
                                    if (position < firstPosition || position > lastPosition)
                                        v.scrollToPosition(position);
                                    v.post(r);
                                } else {
                                    r.run();
                                }
                            }
                        }
                    } else {
                        float radio = (touchY + view.computeVerticalScrollOffset()) / (float) view.computeVerticalScrollRange();
                        for (int i = 0; i < rvPhotos.length; i += 1) {
                            if (i != level - 1) {
                                RecyclerView v = rvPhotos[i];
                                v.scrollBy(0, (int) (v.computeVerticalScrollRange() * radio - touchY - v.computeVerticalScrollOffset()));
                            }
                        }
                    }
                } else {
                    startRadio = touchY / (float) view.getHeight();
                    Logger.d("onScale startRadio:" + startRadio);
                    startRawY = touchY + mPhotoBox.getTop() - svContainer.getScrollY();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mHandler.removeCallbacksAndMessages(null);
                View eventView = null;
                for (View v : rvPhotos) {
                    if (v.getVisibility() != View.VISIBLE) {
                        eventView = v;
                        v.setVisibility(View.VISIBLE);
                    }
                }
                ScaleBoxAdapter.this.onScaleStart();
                if (eventView == null) {
                    event.run();
                } else {
                    eventView.post(event);
                }
            }
        }

        @Override
        public void onScaleEnd(float radio) {
            int maxScale = rvPhotos.length - mDefaultLevel;
            int minScale = 1 - mDefaultLevel;
            radio = Math.max(Math.min(radio - (mDefaultLevel - level), maxScale), minScale);

            int offset = 0;
            float r = radio;
            while (r < -1 || r > 1) {
                if (r < -1) {
                    offset += 1;
                } else {
                    offset -= 1;
                }
                r = radio + offset;
            }

            int lastLevel = level;
            int targetLevel;

            if (r > 0) {
                if (r > 0.5f) {
                    level = mDefaultLevel - offset;
                    targetLevel = mDefaultLevel - offset + 1;
//                    setLevel(mDefaultLevel - offset + 1, r);
                } else {
                    level = mDefaultLevel - offset + 1;
                    targetLevel = mDefaultLevel - offset;
//                    setLevel(mDefaultLevel - offset, r);
                }
            } else {
                if (Math.abs(r) > 0.5f) {
                    level = mDefaultLevel - offset;
                    targetLevel = mDefaultLevel - offset - 1;
//                    setLevel(mDefaultLevel - offset - 1, r);
                } else {
                    level = mDefaultLevel - offset - 1;
                    targetLevel = mDefaultLevel - offset;
//                    setLevel(mDefaultLevel - offset, r);
                }
            }
            setLevel(targetLevel, r, lastLevel != targetLevel);
            ScaleBoxAdapter.this.onScaleEnd();
            if (mOnScaleListener != null)
                mOnScaleListener.onScaleChanged(targetLevel - 1);
        }

        private void setLevel(final int l, float radio, boolean levelChanged) {
            if (Math.abs(radio) > 0 && Math.abs(radio) < 1) {
                showAnim(l - 1);
                if (mSpanCounts.length <= 1) return;

                if (l == rvPhotos.length) {
                    hideAnim(rvPhotos.length - 2,
                            mSpanCounts[rvPhotos.length - 2] / (float) mSpanCounts[l - 1], levelChanged ? (l - 1) : -1);
                } else {
                    int upLevel;
                    if (l == 1) {
                        hideAnim(1, mSpanCounts[1] / (float) mSpanCounts[l - 1], levelChanged ? (l - 1) : -1);
                        upLevel = 2;
                    } else {
                        hideAnim(level - 1, mSpanCounts[level - 1] / (float) mSpanCounts[l - 1], levelChanged ? (l - 1) : -1);
                        upLevel = Math.max(level, l);
                    }
                    if (avoidEdge()) {
                        for (int i = upLevel; i < mSpanCounts.length; i += 1) {
                            hideAnim(i, mSpanCounts[i] / (float) mSpanCounts[l - 1], levelChanged ? (l - 1) : -1);
                        }
                    }
                }
//                if (l == 1) {
//                    hideAnim(1, mSpanCounts[1] / (float) mSpanCounts[l - 1]);
//                } else if (l == rvPhotos.length) {
//                    hideAnim(rvPhotos.length - 2,
//                            mSpanCounts[rvPhotos.length - 2] / (float) mSpanCounts[l - 1]);
//                } else {
//                    hideAnim(level - 1, mSpanCounts[level - 1] / (float) mSpanCounts[l - 1]);
//                    for (int i = Math.max(level, l); i < mSpanCounts.length; i += 1) {
//                        hideAnim(i, mSpanCounts[i] / (float) mSpanCounts[l - 1]);
//                    }
//                }
                if (svContainer != null) {
                    if (l > level) {
                        svContainer.smoothScrollTo(0, (int) toScrollY);
                    } else if (l < level) {
                        svContainer.smoothScrollTo(0, (int) fromScrollY);
                    } else {
                        if (l == 1) {
                            svContainer.smoothScrollTo(0, (int) fromScrollY);
                        } else if (l == 3) {
                            svContainer.smoothScrollTo(0, (int) toScrollY);
                        }
                    }
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setVisibility(l);
                    }
                }, ANIM_DURING);
//                mHandler.postDelayed(() -> setVisibility(l), ANIM_DURING);
            } else {
                setVisibility(l);
                ObjectAnimator.ofPropertyValuesHolder(rvPhotos[l - 1],
                        PropertyValuesHolder.ofFloat("translationX", 0),
                        PropertyValuesHolder.ofFloat("translationY", 0f),
                        PropertyValuesHolder.ofFloat("scaleX", 1f),
                        PropertyValuesHolder.ofFloat("scaleY", 1f))
                        .setDuration(ANIM_DURING)
                        .start();
            }
            level = l;
        }

        private void setVisibility(int l) {
            if (svContainer != null) {
                // for test
//                if (svContainer.getScrollY() + ScreenUtils.getWindowHeight()
//                        > rvPhotos[l - 1].getHeight() + mPhotoBox.getTop()) {
//                    svContainer.smoothScrollTo(0, rvPhotos[l - 1].getHeight() + mPhotoBox.getTop()
//                            - ScreenUtils.getWindowHeight());
//                }
//                mHandler.postDelayed(() -> {
//                    for (int i = 0; i < rvPhotos.length; i++) {
//                        rvPhotos[i].setVisibility(i == l - 1 ? View.VISIBLE : View.INVISIBLE);
//                    }
//                }, 250);
            } else {
                for (int i = 0; i < rvPhotos.length; i++) {
                    rvPhotos[i].setVisibility(i == l - 1 ? View.VISIBLE : View.INVISIBLE);
                }
            }
        }

        private void showAnim(int index) {
            ObjectAnimator.ofPropertyValuesHolder(rvPhotos[index],
                    PropertyValuesHolder.ofFloat("scaleX", 1f),
                    PropertyValuesHolder.ofFloat("scaleY", 1f),
                    PropertyValuesHolder.ofFloat("alpha", 1f),
                    PropertyValuesHolder.ofFloat("translationX", 0f),
                    PropertyValuesHolder.ofFloat("translationY", 0f))
                    .setDuration(ANIM_DURING)
                    .start();
        }

        private void hideAnim(int index, float scale, int pivotIndex) {
            if (pivotIndex < 0) {
                ObjectAnimator.ofPropertyValuesHolder(rvPhotos[index],
                        PropertyValuesHolder.ofFloat("scaleX", scale),
                        PropertyValuesHolder.ofFloat("scaleY", scale),
                        PropertyValuesHolder.ofFloat("alpha", 0)
                )
                        .setDuration(ANIM_DURING)
                        .start();
                return;
            }
            ObjectAnimator.ofPropertyValuesHolder(rvPhotos[index],
                    PropertyValuesHolder.ofFloat("scaleX", scale),
                    PropertyValuesHolder.ofFloat("scaleY", scale),
                    PropertyValuesHolder.ofFloat("alpha", 0),
//                    PropertyValuesHolder.ofFloat("translationX", 0f),
//                    PropertyValuesHolder.ofFloat("translationY", 0f)
                    PropertyValuesHolder.ofFloat("translationX",
                            rvPhotos[index].getTranslationX() - rvPhotos[pivotIndex].getTranslationX()),
                    PropertyValuesHolder.ofFloat("translationY",
                            rvPhotos[index].getTranslationY() - rvPhotos[pivotIndex].getTranslationY())
            )
                    .setDuration(ANIM_DURING)
                    .start();
        }
    }
}
