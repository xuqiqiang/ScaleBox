package com.snailstudio2010.imageviewer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class PhotoViewButton extends FrameLayout {

    public PhotoViewButton(Context context) {
        this(context, null, 0);
    }

    public PhotoViewButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoViewButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setBackgroundResource(R.drawable.selectableItemBackground);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        ViewGroup viewGroup = (ViewGroup) getParent();
//        viewGroup = (ViewGroup) viewGroup.getParent();
//        viewGroup = (ViewGroup) viewGroup.getParent();
        viewGroup.requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(event);
    }
}
