package com.xuqiqiang.uikit.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xuqiqiang.uikit.R;

public class LoadingView extends FrameLayout {

    private final Context context;
    public ViewGroup llError;
    public ImageView ivError;
    public ProgressBar pbLoading;
    public RoundProgressBar rpbLoading;
    public TextView tvError;
    public View cvRetry;
    private Runnable fetchData;
    private View mBindView;
    private Animation mLoadingAnimation;
    private int mImgEmpty = R.mipmap.img_empty_white;
    private int mImgError = R.mipmap.img_error_white;
    private int mImgLoading = R.mipmap.img_loading_white;
    private boolean mIndeterminate = true;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.loading_view, this);
        llError = this;
        llError.setVisibility(View.GONE);
        ivError = view.findViewById(R.id.iv_error);
        pbLoading = view.findViewById(R.id.pb_loading);
        rpbLoading = view.findViewById(R.id.rpb_loading);
        tvError = view.findViewById(R.id.tv_error);
        cvRetry = view.findViewById(R.id.cv_retry);
        cvRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRetry();
            }
        });
    }

    public void bindView(View view) {
        mBindView = view;
    }

    public void setFetchData(Runnable fetchData) {
        this.fetchData = fetchData;
//        if (mBindView != null) mBindView.setVisibility(View.GONE);
//        CustomProgressDialog.show(context, "加载中...", true);
//        fetchData.run();
    }

    public void onRetry() {
        if (fetchData != null) {
//            CustomProgressDialog.show(context, "加载中...", true);
            fetchData.run();
        }
    }

    public void onComplete() {
        stopAnimation();
        llError.setVisibility(View.GONE);
        if (mBindView != null) mBindView.setVisibility(View.VISIBLE);
    }

    public void showEmpty(String tipText) {
        stopAnimation();
        if (mImgEmpty <= 0) {
            ivError.setVisibility(View.GONE);
        } else {
            ivError.setVisibility(View.VISIBLE);
            ivError.setImageResource(mImgEmpty);
        }
        tvError.setText(tipText);
        cvRetry.setVisibility(View.GONE);
        llError.setVisibility(View.VISIBLE);
        if (mBindView != null) mBindView.setVisibility(View.GONE);
    }

    public void setIndeterminate(boolean indeterminate) {
        mIndeterminate = indeterminate;
    }

    private void startAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            ivError.setVisibility(View.GONE);
            pbLoading.setVisibility(View.VISIBLE);

            TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
            ColorStateList colorStateList = ColorStateList.valueOf(typedValue.data);
            pbLoading.setIndeterminateTintList(colorStateList);
            pbLoading.setIndeterminateTintMode(PorterDuff.Mode.SRC_ATOP);
        } else {
            if (mImgLoading <= 0) {
                ivError.setVisibility(View.GONE);
            } else {
                ivError.setVisibility(View.VISIBLE);
                ivError.setImageResource(mImgLoading);
            }
            mLoadingAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
            mLoadingAnimation.setInterpolator(new LinearInterpolator());
            ivError.startAnimation(mLoadingAnimation);
        }
    }

    private void stopAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pbLoading.setIndeterminateTintMode(PorterDuff.Mode.CLEAR);
            pbLoading.setVisibility(View.GONE);
        }
        if (mLoadingAnimation != null) {
            ivError.clearAnimation();
            mLoadingAnimation = null;
        }
        rpbLoading.setVisibility(View.GONE);
    }

    public void showLoading(String tipText) {
        startAnimation();
        tvError.setText(tipText);
        cvRetry.setVisibility(View.GONE);
        llError.setVisibility(View.VISIBLE);
        if (mBindView != null) mBindView.setVisibility(View.GONE);
    }

    public void setProgress(int progress) {
        if (mIndeterminate) {
            mIndeterminate = false;
            stopAnimation();
            rpbLoading.setVisibility(View.VISIBLE);
        }
        rpbLoading.setProgress(progress);
    }

    public void setLoadingText(String tipText) {
        tvError.setText(tipText);
    }

    public void showError(String tipText) {
        stopAnimation();
        if (mImgError <= 0) {
            ivError.setVisibility(View.GONE);
        } else {
            ivError.setVisibility(View.VISIBLE);
            ivError.setImageResource(mImgError);
        }
        tvError.setText(tipText);
        cvRetry.setVisibility(View.VISIBLE);
        llError.setVisibility(View.VISIBLE);
        if (mBindView != null) mBindView.setVisibility(View.GONE);
    }

    public void showError(String tipText, Runnable retry) {
        showError(tipText);
        this.fetchData = retry;
    }

    public boolean isShowing() {
        return llError.getVisibility() == View.VISIBLE;
    }

    public void setImgEmpty(int imgEmpty) {
        this.mImgEmpty = imgEmpty;
    }

    public void setImgError(int imgError) {
        this.mImgError = imgError;
    }

    public void setImgLoading(int imgLoading) {
        this.mImgLoading = imgLoading;
    }
}
