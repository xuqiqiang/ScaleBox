package com.xuqiqiang.uikit.view;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.lang.reflect.Field;

public class ViewPagerScroller extends Scroller {
    private int mDuration = 1000;/*default duration time*/

    public ViewPagerScroller(Context context) {
        super(context);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    /**
     * Set custom duration time.
     *
     * @param duration duration
     */
    public void setScrollDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Get duration time.
     *
     * @return duration
     */
    public int getmDuration() {
        return mDuration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        //此处必须重写，网上有些资料里只重写了上面那个，不知道他们的是怎么工作的，我实际测试时行不通的。
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    public void initViewPagerScroll(ViewPager pager) {
        try {
            Field field = ViewPager.class.getDeclaredField("mScroller");
            field.setAccessible(true);
            field.set(pager, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
