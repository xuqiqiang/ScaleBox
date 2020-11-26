package com.xuqiqiang.uikit.view;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.xuqiqiang.uikit.utils.SingleTaskHandler;
import com.xuqiqiang.uikit.R;

public class TouchOpacity extends RelativeLayout {

    private static final int OPACITY_DEFAULT = 70;
    private boolean enabled = true;
    private float opacityValue;
    private SingleTaskHandler mSingleTaskHandler = new SingleTaskHandler(Looper.getMainLooper());
    private int mSecurityTime = 300;

    public TouchOpacity(Context context) {
        this(context, null, 0);
    }

    public TouchOpacity(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchOpacity(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.TouchOpacity, 0, 0);

        int opacity = typedArray.getInt(R.styleable.TouchOpacity_opacity, OPACITY_DEFAULT);
        boolean enabled = typedArray.getBoolean(R.styleable.TouchOpacity_enabled, true);
        mSecurityTime = typedArray.getInt(R.styleable.TouchOpacity_security_time, mSecurityTime);
        typedArray.recycle();
        if (opacity < 0 || opacity > 100) opacity = OPACITY_DEFAULT;
        opacityValue = opacity / 100f;
        if (!enabled) setEnabled(false);
    }

    @Override
    public void setOnClickListener(@Nullable final OnClickListener l) {
        if (mSecurityTime <= 0) {
            super.setOnClickListener(l);
            return;
        }
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                mSingleTaskHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (l != null) l.onClick(v);
                    }
                }, mSecurityTime);
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.enabled = enabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!this.enabled) return super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, opacityValue);
            ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(this, alpha);
            scale.setDuration(200);
            scale.start();
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", opacityValue, 1.0f);
            ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(this, alpha);
            scale.setDuration(200);
            scale.start();
        }
        return super.onTouchEvent(event);
    }

}
