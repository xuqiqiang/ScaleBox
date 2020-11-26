package com.xuqiqiang.uikit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.xuqiqiang.uikit.utils.DisplayUtils;
import com.xuqiqiang.uikit.R;

public class PagerIndicator extends FrameLayout {

    private static final String TAG = "PagerIndicator";
    private Context mContext;
    private ViewGroup mDotContainer;
    private ImageView mDarkDot;
    private int mDistance;
    private int mDotDarkResId;
    private int mDotLightResId;
    private int mDotMarginDimen;

    public PagerIndicator(Context context) {
        this(context, null, 0);
    }

    public PagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PagerIndicator);
        mDotDarkResId = typedArray.getResourceId(R.styleable.PagerIndicator_pi_dot_dark, R.drawable.dot_dark);
        mDotLightResId = typedArray.getResourceId(R.styleable.PagerIndicator_pi_dot_light, R.drawable.dot_light);
        mDotMarginDimen = typedArray.getDimensionPixelSize(R.styleable.PagerIndicator_pi_dot_margin,
                (int) DisplayUtils.dip2px(context, 8));
        typedArray.recycle();

        mDotContainer = new LinearLayout(context);
        ((LinearLayout) mDotContainer).setOrientation(LinearLayout.HORIZONTAL);
        addView(mDotContainer, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mDarkDot = new ImageView(context);
        mDarkDot.setImageResource(mDotDarkResId);
        addView(mDarkDot, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setVisibility(View.GONE);
    }

    public void bind(final ViewPager viewPager) {
        initViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //页面滚动时小白点移动的距离，并通过setLayoutParams(params)不断更新其位置
                float leftMargin = mDistance * (position + positionOffset);
                LayoutParams params = (LayoutParams) mDarkDot.getLayoutParams();
                params.leftMargin = (int) leftMargin;
                mDarkDot.setLayoutParams(params);
//                if (position == 1) {
//                    mBtn_next.setVisibility(View.VISIBLE);
//                }
//                if (position != 1 && mBtn_next.getVisibility() == View.VISIBLE) {
//                    mBtn_next.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onPageSelected(int position) {
//                //页面跳转时，设置小圆点的margin
//                float leftMargin = mDistance * position;
//                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLight_dots.getLayoutParams();
//                params.leftMargin = (int) leftMargin;
//                mLight_dots.setLayoutParams(params);
//                if (position == 1) {
//                    mBtn_next.setVisibility(View.VISIBLE);
//                }
//                if (position != 1 && mBtn_next.getVisibility() == View.VISIBLE) {
//                    mBtn_next.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.addOnAdapterChangeListener(new ViewPager.OnAdapterChangeListener() {
            @Override
            public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {
                Log.d(TAG, "onAdapterChanged:" + (oldAdapter != null) + "," + (newAdapter != null));
                initViewPager(viewPager);
            }
        });
    }

    private void initViewPager(final ViewPager viewPager) {
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) return;
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                initViewPager(viewPager);
            }
        });
        if (adapter.getCount() <= 1) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        mDotContainer.removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            ImageView dot = new ImageView(mContext);
            dot.setImageResource(mDotLightResId);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i < adapter.getCount() - 1)
                layoutParams.setMargins(0, 0, mDotMarginDimen, 0);
            else
                layoutParams.setMargins(0, 0, 0, 0);
            mDotContainer.addView(dot, layoutParams);
            final int index = i;
            dot.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPager.setCurrentItem(index);
                }
            });
//            dot.setOnClickListener(view -> viewPager.setCurrentItem(index));
        }

        if (mDistance == 0) {
            mDarkDot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    //获得两个圆点之间的距离
                    mDistance = mDotContainer.getChildAt(1).getLeft() - mDotContainer.getChildAt(0).getLeft();
                    mDarkDot.getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }
            });
        }
    }
}
