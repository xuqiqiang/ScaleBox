package com.xuqiqiang.uikit.fragment;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends PagerAdapter {
    private final List<? extends View> mViewList;

    public ViewPagerAdapter(List<? extends View> mViewList) {
        this.mViewList = mViewList;
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    public View getItem(int position) {
        return mViewList.get(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public void finishUpdate(@NonNull ViewGroup container) {
        // Googlers it is shame that the bug is there in view pager more than 3 years.  Why don't you take seriously? Fix it guys can't wait more time
        try {
            super.finishUpdate(container);
        } catch (NullPointerException ignored) {
        }
    }
}
