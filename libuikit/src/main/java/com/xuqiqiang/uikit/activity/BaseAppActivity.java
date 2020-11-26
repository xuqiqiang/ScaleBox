package com.xuqiqiang.uikit.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.utils.ApplicationUtils;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.view.CustomProgressDialog;
import com.xuqiqiang.uikit.view.ToastMaster;
import com.xuqiqiang.uikit.view.menu.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import static com.xuqiqiang.uikit.utils.DisplayUtils.attrResId;
import static com.xuqiqiang.uikit.utils.Utils.mMainHandler;

/**
 * Created by xuqiqiang on 2019/08/19.
 */
public abstract class BaseAppActivity extends BaseThemeActivity {
    public static final int RESULT_UNKNOWN_ERROR = -99;
    private static final String TAG = "BaseAppActivity";

    protected View mBtnTitleBack;
    protected View mBtnTitleMore;
    protected PopupMenu mPopupMenu;
    protected TextView mTitleText;
    private @LayoutRes
    int mLayoutResID;
    private boolean isRunning;
    private boolean isPaused;
    private final List<Runnable> mRunnablesAfterResume = new ArrayList<>();
    private int mColorTitle;
    private int mStatusBarDarkTheme;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationUtils.restartApp(this);
            return;
        }
        try {
            mLayoutResID = initView(savedInstanceState);
            if (mLayoutResID != 0) {
                if (!useStatusBarImmersion() || !useStatusBarWrapper()) {
                    setContentView(mLayoutResID);
                } else {
                    setContentView(R.layout.activity_wrapper);
                    LayoutInflater mInflater = getLayoutInflater();
                    ViewGroup wrapper = findViewById(R.id.activity_wrapper);
                    mInflater.inflate(mLayoutResID, wrapper);
                    View statusBarView = findViewById(R.id.status_bar_view);
                    statusBarView.setBackgroundColor(colorTitle());
                }

                int contentViewId = initContentView(savedInstanceState);
                if (contentViewId != 0) setView(contentViewId);
                else {
                    View view = initContentInflateView(savedInstanceState);
                    if (view != null) setView(view);
                }
                onInitView(savedInstanceState);
            }
            if (useTitleBar()) {
                if (checkTitleBar()) {
                    initTitleBar();
                } else {
                    hideTitleBar();
                }
            }
        } catch (Exception e) {
            if (e instanceof InflateException) throw e;
            e.printStackTrace();
        }
        if (hideSoftInput()) {
            mKeyboardManager.setKeyboardHidden();
        }
        isRunning = true;
        int lazy = checkLazy();
        Log.d(TAG, "lazy" + lazy);
        if (lazy > 0) {
            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isRunning)
                        initData(savedInstanceState);
                }
            }, lazy);
        } else {
            initData(savedInstanceState);
        }
    }

    public void initData(@Nullable Bundle savedInstanceState) {
    }

    protected boolean useStatusBarWrapper() {
        return false;
    }

    //    @Override
    public @LayoutRes
    int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_base;
    }

    protected int initContentView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    protected View initContentInflateView(@Nullable Bundle savedInstanceState) {
        return null;
    }

    protected void onInitView(@Nullable Bundle savedInstanceState) {
    }

    protected void setView(int layoutResID) {
        LayoutInflater mInflater = getLayoutInflater();
        View view = mInflater.inflate(layoutResID, null);
        setView(view);
    }

    protected void setView(View view) {
        FrameLayout flContent = findViewById(R.id.fl_content);
        flContent.removeAllViews();
        flContent.addView(view, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                fillContent() ? LinearLayout.LayoutParams.MATCH_PARENT :
                        ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    protected boolean fillContent() {
        return true;
    }

    protected void setFragment(Fragment fragment) {
        setFragment(R.id.fl_content, fragment);
    }

    protected void setFragment(int id, Fragment fragment) {
        try {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            FragmentTransaction mFragmentTransaction = supportFragmentManager.beginTransaction();
            mFragmentTransaction.add(id, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
        if (!ArrayUtils.isEmpty(mRunnablesAfterResume)) {
            for (Runnable r : mRunnablesAfterResume) r.run();
            mRunnablesAfterResume.clear();
        }
    }

    public void postOnResume(Runnable r) {
        if (isPaused)
            mRunnablesAfterResume.add(r);
        else
            r.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    public boolean isPaused() {
        return isPaused;
    }

    private int checkLazy() {
        return ApplicationUtils.getActivityMetaDataInt(this, "lazy");
    }

    protected boolean hideSoftInput() {
        return false;
    }

    protected boolean useTitleBar() {
        return mLayoutResID == R.layout.activity_base;
    }

    private boolean checkTitleBar() {
        String msg = ApplicationUtils.getActivityMetaData(this, "showTitleBar");
        return !"no".equalsIgnoreCase(msg);
    }

    protected void initTitleBar() {
        mTitleText = findViewById(R.id.title);

        initTitle();

        mBtnTitleBack = findViewById(R.id.btn_title_back);
//        btn_title_back.setOnClickListener(arg -> onTitleBack());
        if (mBtnTitleBack != null) {
            mBtnTitleBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTitleBack();
                }
            });
//            ImageView ivTitleBack = findViewById(R.id.iv_title_back);
//            if (ivTitleBack != null) {
//                int icBack = attrResId(this, R.attr.icBack);
//                Log.d(TAG, "icBack.data:" + icBack);
//                if (icBack != 0) {
//                    ivTitleBack.setImageResource(icBack);
//                } else {
//                    ivTitleBack.setImageResource(R.mipmap.ic_back);
//                }
//            }
        }

        mPopupMenu = initPopupMenu();
        if (mPopupMenu != null) {
//            if (!useMenuText()) {
//                int icMenu = attrResId(this, R.attr.icMenu);
//                ImageView ivTitleMenu = findViewById(R.id.iv_title_menu);
//                if (icMenu != 0) {
//                    ivTitleMenu.setImageResource(icMenu);
//                } else {
//                    ivTitleMenu.setImageResource(R.mipmap.ic_more);
//                }
//            }
            mBtnTitleMore = findViewById(useMenuText() ? R.id.btn_title_menu_text : R.id.btn_title_menu);
            mBtnTitleMore.setVisibility(View.VISIBLE);
            mBtnTitleMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPopupMenu.show();
                }
            });
        }

        View titleLine = findViewById(R.id.title_line);
        if (titleLine != null && showTitleLine()) {
            titleLine.setVisibility(View.VISIBLE);
        }
    }

    protected PopupMenu initPopupMenu() {
        return null;
    }

    protected boolean useMenuText() {
        return false;
    }

    protected boolean showTitleLine() {
        return false;
    }

    protected void hideTitleBar() {
        ViewGroup titlebar = findViewById(R.id.titlebar);
        titlebar.setVisibility(View.GONE);
    }

    protected void setTitleMenuText(String name, View.OnClickListener listener) {
        setTitleMenuText(name, 0, 0, listener);
    }

    protected void setTitleMenuText(String name, float size, int color, View.OnClickListener listener) {
        ViewGroup btnTitleOption = findViewById(R.id.btn_title_menu_text);
        btnTitleOption.setVisibility(View.VISIBLE);
        TextView tvOption = findViewById(R.id.tv_menu);
        tvOption.setText(name);
        if (size > 0) tvOption.setTextSize(size);
        if (color != 0) tvOption.setTextColor(color);
        btnTitleOption.setOnClickListener(listener);
    }

    protected void setTitleMenuImg(@DrawableRes int imgId, View.OnClickListener listener) {
        ViewGroup btnTitleOption = findViewById(R.id.btn_title_menu);
        btnTitleOption.setVisibility(View.VISIBLE);
        ImageView tvOption = findViewById(R.id.iv_title_menu);
        tvOption.setImageResource(imgId);
        btnTitleOption.setOnClickListener(listener);
    }

    protected void initTitle() {
        setTitle(getTitle());
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (mTitleText != null)
            mTitleText.setText(title);
    }

    public void setTitleLength(int length) {
        if (mTitleText != null)
            mTitleText.setMaxEms(length);
    }

    protected void setOnTitleTextClickListener(View.OnClickListener listener) {
        if (mTitleText == null) return;
        mTitleText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mTitleText.setTextColor(Color.RED);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mTitleText.setTextColor(Color.WHITE);
                }
                return false;
            }
        });
        mTitleText.setOnClickListener(listener);
    }

    protected void onTitleBack() {
        onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode : " + requestCode + ", resultCode : " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_UNKNOWN_ERROR) {
            showMessage(R.string.unknown_error);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onTitleBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        this.isRunning = false;
        try {
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Activity getActivity() {
        return this;
    }

    public void displayProgress(String text) {
        CustomProgressDialog.show(this, text, true);
    }

    public void displayProgress(int textId) {
        CustomProgressDialog.show(this, getString(textId), true);
    }

    public void dismissProgress() {
        CustomProgressDialog.close();
    }

    public void showMessage(@NonNull String message) {
        ToastMaster.showToast(this, message);
    }

    public void showMessage(int messageId) {
        ToastMaster.showToast(this, messageId);
    }
}
