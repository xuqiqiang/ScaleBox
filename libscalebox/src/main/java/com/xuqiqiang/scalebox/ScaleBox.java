package com.xuqiqiang.scalebox;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.core.widget.NestedScrollView;

import com.xuqiqiang.scalebox.utils.Logger;

/**
 * Created by xuqiqiang on 2020/10/17.
 */
public class ScaleBox extends FrameLayout {
    OnScaleListener mOnScaleListener;
    private boolean isMultiPointer;
    private boolean isTouchStart;
    private float mFingerSpacing = -1;
    private float mStartCenterX = -1;
    private float mStartCenterY = -1;
    private OnScaleEventListener mOnScaleEventListener;
    private float mRadio;
    private NestedScrollView mScrollView;
    private ScaleBoxAdapter mAdapter;

    public ScaleBox(Context context) {
        this(context, null, 0);
    }

    public ScaleBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAdapter(ScaleBoxAdapter adapter) {
        this.mAdapter = adapter;
        mAdapter.init(this);
    }

    void setOnScaleListener(OnScaleListener listener) {
        this.mOnScaleListener = listener;
    }

    public void setOnScaleEventListener(OnScaleEventListener listener) {
        this.mOnScaleEventListener = listener;
    }

    public boolean isMultiPointer() {
        return isMultiPointer;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Logger.d("onTouch:" + event.getPointerCount() + "," + event.getAction());
        if (mAdapter == null) return super.dispatchTouchEvent(event);
        if (event.getPointerCount() == 1 && event.getAction() == MotionEvent.ACTION_DOWN) {
            mFingerSpacing = -1;
            isMultiPointer = false;
            isTouchStart = false;
        }
        if (event.getPointerCount() > 1) {
            if (isMultiPointer) {
                if (isTouchStart) {
                    if (mFingerSpacing < 0) {
                        mFingerSpacing = getFingerSpacing(event);
                        mStartCenterX = (event.getX(0) + event.getX(1)) / 2f;
                        mStartCenterY = (event.getY(0) + event.getY(1)) / 2f;
                    } else {
                        float currentFingerSpacing = getFingerSpacing(event);
                        Logger.d("getFingerSpacing:" + (mFingerSpacing - currentFingerSpacing));

                        mRadio = (mFingerSpacing - currentFingerSpacing) / mAdapter.getTouchGap();
                        float centerX = (event.getX(0) + event.getX(1)) / 2f;
                        float centerY = (event.getY(0) + event.getY(1)) / 2f;
                        if (mOnScaleListener != null)
                            mOnScaleListener.onScale(mRadio, centerX - mStartCenterX
                                    , centerY - mStartCenterY);
                    }
                }
            } else {
                if (mOnScaleListener != null) {
                    if (mOnScaleEventListener != null) mOnScaleEventListener.onBeforeEvent();
                    mOnScaleListener.onScaleStart(
                            (event.getX(0) + event.getX(1)) / 2f,
                            (event.getY(0) + event.getY(1)) / 2f,
                            new Runnable() {
                                @Override
                                public void run() {
                                    isTouchStart = true;
                                    if (mOnScaleEventListener != null)
                                        mOnScaleEventListener.onAfterEvent();
                                }
                            });
                }


                if (mScrollView == null) {
                    ViewParent parent = getParent();
                    while (!(parent instanceof NestedScrollView) && parent != null) {
                        parent = parent.getParent();
                    }
                    mScrollView = (NestedScrollView) parent;
                }
                if (mScrollView != null)
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                event.setAction(MotionEvent.ACTION_CANCEL);
                super.dispatchTouchEvent(event);
            }

            isMultiPointer = true;
            return true;
        } else {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (isMultiPointer) {
                    if (mOnScaleListener != null)
                        mOnScaleListener.onScaleEnd(mRadio);
                    mFingerSpacing = -1;
                    isMultiPointer = false;
                }
            }
        }
        if (isMultiPointer) return true;
        super.dispatchTouchEvent(event);
        return true;
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public interface OnScaleListener {
        void onScale(float radio, float offsetX, float offsetY);

        void onScaleStart(float touchX, float touchY, Runnable event);

        void onScaleEnd(float radio);
    }

    public interface OnScaleEventListener {
        void onBeforeEvent();

        void onAfterEvent();
    }

    public interface IGalleryAdapter {
        void notifyMockItemInserted(int itemCount);

        int getOffsetCount();

        void resetOffsetCount();
    }
}
