package com.xuqiqiang.uikit.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.xuqiqiang.uikit.utils.KeyboardManager;
import com.xuqiqiang.uikit.utils.MathUtils;
import com.xuqiqiang.uikit.utils.ViewUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public abstract class BaseAppCompatActivity extends AppCompatActivity {

    private static final String TAG = BaseAppCompatActivity.class.getSimpleName();
    protected KeyboardManager mKeyboardManager;
    private View mDimView;
    private ObjectAnimator mDimViewAnim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Fix error: Only fullscreen activities can request orientation
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            boolean result = fixOrientation();
            Log.i(TAG, "onCreate fixOrientation when Oreo, result = " + result);
        }
        super.onCreate(savedInstanceState);
        mKeyboardManager = new KeyboardManager(this);
        if (isFullScreen()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            ActivityUtils.setFullScreen(getWindow());
        }
    }

    private boolean isTranslucentOrFloating() {
        boolean isTranslucentOrFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray ta = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentOrFloating = (boolean) m.invoke(null, ta);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentOrFloating;
    }

    private boolean fixOrientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = -1;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentOrFloating()) {
            Log.i(TAG, "avoid calling setRequestedOrientation when Oreo.");
            return;
        }
        super.setRequestedOrientation(requestedOrientation);
    }

    protected boolean isFullScreen() {
        return false;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && isFullScreen()) {
            ViewUtils.fullScreenImmersive(getWindow());
        }
    }

    @Override
    protected void onStart() {
        //Fix: java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Object[]
        try {
            super.onStart();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mKeyboardManager != null) {
            mKeyboardManager.onDestroy();
            mKeyboardManager = null;
        }
        if (mDimViewAnim != null && mDimViewAnim.isRunning()) {
            mDimViewAnim.removeAllListeners();
            mDimViewAnim.cancel();
        }
        try {
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                boolean res = mKeyboardManager.hiddenKeyboard(v);
                if (res) {
                    return true;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 键盘弹出时，点击非键盘位置，首先隐藏键盘
     */
    protected boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public void setDimAmount(float amount) {
        setDimAmount(Color.BLACK, amount);
    }

    /**
     * Set the amount of dim behind the window. This overrides
     * the default dim amount of that is selected by the Window based on
     * its theme.
     *
     * @param amount The new dim amount, from 0 for no dim to 1 for full dim.
     */
    public void setDimAmount(int color, float amount) {
        if (mDimView == null) {
            mDimView = new View(this);
//            mDimView.setBackgroundColor(Color.BLACK);
            mDimView.setAlpha(0);
            ViewUtils.addTopView(this, mDimView);
        }
        mDimView.bringToFront();
        mDimView.setBackgroundColor(color);

        if (mDimViewAnim != null && mDimViewAnim.isRunning()) {
            mDimViewAnim.removeAllListeners();
            mDimViewAnim.cancel();
        }
        if (amount > 0) mDimView.setVisibility(View.VISIBLE);
        mDimViewAnim = ObjectAnimator.ofFloat(mDimView, "alpha", amount);
        mDimViewAnim.setDuration(300);
        if (MathUtils.eq0(amount)) {
            mDimViewAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mDimView.setVisibility(View.INVISIBLE);
                }
            });
        }
        mDimViewAnim.start();
    }

    public KeyboardManager getKeyboardManager() {
        return mKeyboardManager;
    }
}
