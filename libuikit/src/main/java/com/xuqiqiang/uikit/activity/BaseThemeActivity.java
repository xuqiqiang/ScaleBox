package com.xuqiqiang.uikit.activity;

import androidx.annotation.ColorInt;

import com.xuqiqiang.uikit.R;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.utils.StatusBarUtils;
import com.xuqiqiang.uikit.utils.StringUtils;

import static com.xuqiqiang.uikit.utils.DisplayUtils.attrData;

/**
 * Created by xuqiqiang on 2020/07/12.
 */
public class BaseThemeActivity extends BaseAppCompatActivity {
    private static final String TAG = BaseThemeActivity.class.getSimpleName();
    private int mColorTitle;
    private int mStatusBarDarkTheme;

    @Override
    protected void onResume() {
        super.onResume();
        initStatusBar();
    }

    protected void initStatusBar() {
        boolean statusBarImmersion = useStatusBarImmersion();
        StatusBarUtils.setRootViewFitsSystemWindows(this, !statusBarImmersion);
        if (mColorTitle == 0) mColorTitle = colorTitle();
//        int colorTitle = colorTitle();
        Logger.d(TAG, "initStatusBar colorTitle:" + StringUtils.numToHex16(mColorTitle));
        if (statusBarImmersion) {
            StatusBarUtils.setTranslucentStatus(this);
        } else {
            StatusBarUtils.setStatusBarColor(this, mColorTitle);
        }
        StatusBarUtils.setNavigationBarColor(this, mColorTitle);
        StatusBarUtils.setStatusBarDarkTheme(this, statusBarDark());
    }

    /**
     * @return 沉浸式状态栏
     */
    protected boolean useStatusBarImmersion() {
        int statusBarImmersion = attrData(this, R.attr.statusBarImmersion);
        Logger.d(TAG, "statusBarImmersion:" + (statusBarImmersion != 0));
        return statusBarImmersion != 0;
    }

    /**
     * @return 状态栏和底部导航栏背景颜色
     */
    @ColorInt
    protected int colorTitle() {
        if (mColorTitle != 0) return mColorTitle;
        return attrData(this, R.attr.colorPrimaryDark);
    }

    public void setColorTitle(int color) {
        mColorTitle = color;
        StatusBarUtils.setNavigationBarColor(this, color);
        if (!useStatusBarImmersion()) {
            StatusBarUtils.setStatusBarColor(this, color);
        }
    }

    /**
     * @return 状态栏黑底白字
     */
    protected boolean statusBarDark() {
        if (mStatusBarDarkTheme == 1) return true;
        else if (mStatusBarDarkTheme == 2) return false;
        int statusBarDark = attrData(this, R.attr.statusBarDark);
        Logger.d(TAG, "statusBarDark:" + (statusBarDark != 0));
        return statusBarDark != 0;
    }

    public boolean setStatusBarDarkTheme(boolean dark) {
        mStatusBarDarkTheme = dark ? 1 : 2;
        return StatusBarUtils.setStatusBarDarkTheme(this, statusBarDark());
    }
}
