package com.xuqiqiang.scalebox.demo.view.component;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.scalebox.utils.Utils;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

/**
 * Created by xuqiqiang on 2020/07/07.
 */
public class FastScrollerHelper {
    private final Handler mHandler;
    private final VerticalRecyclerViewFastScroller mFastScroller;
    private RecyclerView mBindRecyclerView;
    private ObjectAnimator mAnimatorHideScroller;
    private final Runnable mRunnableHideScroller = new Runnable() {
        @Override
        public void run() {
            if (mAnimatorHideScroller != null)
                mAnimatorHideScroller.cancel();
            mAnimatorHideScroller = ObjectAnimator.ofPropertyValuesHolder(mFastScroller,
                    PropertyValuesHolder.ofFloat("alpha", 0),
                    PropertyValuesHolder.ofFloat("translationX",
                            Utils.dip2px(mFastScroller.getContext(), 20))
            ).setDuration(300);
            mAnimatorHideScroller.start();
        }
    };
    private RecyclerView.OnScrollListener mFastDisplayListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                hideFastScroller();
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                showFastScroller();
            }
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    public FastScrollerHelper(VerticalRecyclerViewFastScroller fastScroller) {
        this.mFastScroller = fastScroller;
        this.mHandler = new Handler(Looper.getMainLooper());
        mFastScroller.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showFastScroller();
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    hideFastScroller();
                }
                event.setLocation(event.getX(), event.getY()
                        - Utils.dip2px(mFastScroller.getContext(), 25));
                float scrollProgress = mFastScroller.getScrollProgress(event);
                mFastScroller.scrollTo(scrollProgress, true);
                mFastScroller.moveHandleToPosition(scrollProgress);
                return true;
            }
        });
    }

    public void bindRecyclerView(@NonNull RecyclerView recyclerView) {
        if (mBindRecyclerView != null) {
            mBindRecyclerView.removeOnScrollListener(mFastScroller.getOnScrollListener());
            recyclerView.removeOnScrollListener(mFastDisplayListener);
        }
        mBindRecyclerView = recyclerView;
        mFastScroller.setRecyclerView(recyclerView);
        // Connect the scroller to the recycler (to let the recycler scroll the scroller's handle)
        recyclerView.addOnScrollListener(mFastScroller.getOnScrollListener());
        recyclerView.addOnScrollListener(mFastDisplayListener);
    }

    public void showFastScroller() {
        if (mAnimatorHideScroller != null && mAnimatorHideScroller.isRunning())
            mAnimatorHideScroller.cancel();
        mHandler.removeCallbacks(mRunnableHideScroller);
        if (mFastScroller.getTranslationX() > 0) {
            mFastScroller.setTranslationX(0);
            mFastScroller.setAlpha(1f);
        }
    }

    public void hideFastScroller() {
        mHandler.postDelayed(mRunnableHideScroller, 2000);
    }
}