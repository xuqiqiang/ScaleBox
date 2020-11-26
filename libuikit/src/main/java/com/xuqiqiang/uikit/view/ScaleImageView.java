package com.xuqiqiang.uikit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.xuqiqiang.uikit.R;

/**
 * 支持centerTopCrop和centerBottomCrop的ImageView
 * <p>
 * Created by xuqiqiang on 2020/05/17.
 */
public class ScaleImageView extends AppCompatImageView {

    private Drawable mDrawable;
    private int mDrawableWidth;
    private int mDrawableHeight;
    private boolean mDrawableChangde;
    private int scaleType;

    public ScaleImageView(Context context) {
        this(context, null, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setScaleType(ScaleType.MATRIX);
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ScaleImageView);
            scaleType = typedArray.getInt(R.styleable.ScaleImageView_siv_scaleType, 0);
            typedArray.recycle();
        }
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mDrawable != drawable) mDrawableChangde = true;
        mDrawable = drawable;
        if (drawable != null) {
            mDrawableWidth = drawable.getIntrinsicWidth();
            mDrawableHeight = drawable.getIntrinsicHeight();
        } else {
            mDrawableWidth = mDrawableHeight = -1;
        }
//        configureBounds();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        final boolean changed = super.setFrame(l, t, r, b);
//        mHaveFrame = true;
        configureBounds();
//        final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
//        final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
//        Logger.d("ScaleImageView size2:" + vwidth + "," + vheight);
//        Logger.d("ScaleImageView draw2:" + mDrawableWidth + "," + mDrawableHeight);
        return changed;
    }

    // centerBottomCrop
    private void configureBounds() {
        if (!mDrawableChangde) {
            return;
        }
//        Logger.d("ScaleImageView configureBounds");
        mDrawableChangde = false;

        final int dwidth = mDrawableWidth;
        final int dheight = mDrawableHeight;

        final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

        float scale;
        float dx = 0, dy = 0;

        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight;
            dx = (vwidth - dwidth * scale) * 0.5f;
        } else {
            scale = (float) vwidth / (float) dwidth;
            if (scaleType == 0) {
                dy = vheight - dheight * scale;
            }
        }

        Matrix mDrawMatrix = new Matrix();
        mDrawMatrix.setScale(scale, scale);
        mDrawMatrix.postTranslate(Math.round(dx), Math.round(dy));
        setImageMatrix(mDrawMatrix);
    }
}