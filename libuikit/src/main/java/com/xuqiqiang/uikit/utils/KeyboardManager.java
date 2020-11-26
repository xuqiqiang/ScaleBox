package com.xuqiqiang.uikit.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class KeyboardManager {
    private static final String TAG = "KeyboardManager";

    private Activity context;
    private InputMethodManager mInputMethodManager;

    private int mLastBottom = -1;

    private boolean isInitOnGlobalLayoutListener;
    private List<OnKeyboardStatusChangeListener> mListeners;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private Rect mWindowPort;
    private Rect mWindowPortVertical;
    private Rect mWindowPortHorizontal;

    public KeyboardManager(Activity context) {
        this.context = context;
        mInputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public static void showKeyboard(Activity context, View view) {
        new KeyboardManager(context).showKeyboard(view);
    }

    public static void hiddenKeyboard(Activity context) {
        new KeyboardManager(context).hiddenKeyboard();
    }

    public static boolean hiddenKeyboard(Activity context, View view) {
        return new KeyboardManager(context).hiddenKeyboard(view);
    }

    public KeyboardManager setKeyboardHidden() {
        context.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return this;
    }

    public boolean hiddenKeyboard() {
        return hiddenKeyboard(context.getWindow().getDecorView());
    }

    public boolean hiddenKeyboard(View view) {
        if (mInputMethodManager != null && view != null)
            return mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);// InputMethodManager.HIDE_NOT_ALWAYS);
        return false;
    }

    public void showKeyboard(final View view) {
        if (view instanceof EditText) {
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mInputMethodManager != null && view != null)
                    mInputMethodManager.showSoftInput(view,
                            InputMethodManager.RESULT_SHOWN);
            }
        }, 200);
    }

    public boolean isShowing() {
        Rect rect = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.bottom < ScreenUtils.getWindowHeight() * 0.8f;
    }

    public void addOnKeyboardStatusChangeListener(OnKeyboardStatusChangeListener listener) {
        if (listener == null) return;
        if (mListeners == null) mListeners = new ArrayList<>();
        mListeners.add(listener);

        if (!isInitOnGlobalLayoutListener) {
            isInitOnGlobalLayoutListener = true;
            final View rootView = context.getWindow().getDecorView();
            mGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    rootView.getWindowVisibleDisplayFrame(r);
                    int bottom = r.bottom;
                    if (bottom == mLastBottom) {
                        return;
                    }

                    if (mWindowPort != null) {
                        if ((r.right - r.bottom) * (mWindowPort.right - mWindowPort.bottom) < 0) {
                            mLastBottom = -1;
                            mWindowPort = null;
                        }
                    }

                    if (mWindowPort == null) {
                        if (r.right < r.bottom) {
                            if (mWindowPortVertical != null) mWindowPort = mWindowPortVertical;
                            else if (r.bottom >= ScreenUtils.getViewportHeight(context)) {
                                mWindowPort = r;
                                mWindowPortVertical = r;
                            }
                        } else if (r.right > r.bottom) {
                            if (mWindowPortHorizontal != null) mWindowPort = mWindowPortHorizontal;
                            else if (r.bottom >= ScreenUtils.getWidth()
                                    - StatusBarUtils.getStatusBarHeight(context)
                                    - ScreenUtils.getNavigationBarHeightIfRoom(context)) {
                                mWindowPort = r;
                                mWindowPortHorizontal = r;
                            }
                        }
                        if (mWindowPort == null) return;
                    }

                    if (mLastBottom == -1) {
                        mLastBottom = bottom;
                        return;
                    }
                    mLastBottom = bottom;
                    if (!ArrayUtils.isEmpty(mListeners)) {
                        for (OnKeyboardStatusChangeListener l : mListeners) {
                            l.onKeyboardStatusChange(bottom < mWindowPort.bottom,
                                    mWindowPort.bottom - bottom);
                        }
                    }
                }
            };
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        }
    }

    public void removeOnKeyboardStatusChangeListener(
            OnKeyboardStatusChangeListener listener) {
        if (mListeners != null)
            mListeners.remove(listener);
    }

    public void onDestroy() {
        View rootView = context.getWindow().getDecorView();
        if (mGlobalLayoutListener != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
            mGlobalLayoutListener = null;
        }
        hiddenKeyboard(rootView);
        if (mListeners != null) {
            mListeners.clear();
            mListeners = null;
        }
        mInputMethodManager = null;
        context = null;
    }

    public interface OnKeyboardStatusChangeListener {
        public void onKeyboardStatusChange(boolean isShow, int keyboardHeight);
    }
}
