package com.xuqiqiang.uikit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.utils.StatusBarUtils;

public class StatusBarHeightView extends FrameLayout {
    private static final int TYPE_USE_HEIGHT = 0, TYPE_USE_PADDING_TOP = 1;
    private int statusBarHeight;
    private int type;

    public StatusBarHeightView(Context context) {
        this(context, null, 0);
    }

    public StatusBarHeightView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusBarHeightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            statusBarHeight = StatusBarUtils.getStatusBarHeight(context);
        } else {
            statusBarHeight = 0;
        }
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.StatusBarHeightView);
            type = typedArray.getInt(R.styleable.StatusBarHeightView_use_type, TYPE_USE_HEIGHT);
            typedArray.recycle();
        }
        if (type == TYPE_USE_PADDING_TOP) {
            setPadding(getPaddingLeft(), statusBarHeight, getPaddingRight(), getPaddingBottom());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (type == TYPE_USE_HEIGHT) {
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                    statusBarHeight);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}