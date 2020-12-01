package com.xuqiqiang.uikit.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.xuqiqiang.uikit.utils.ApplicationUtils;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.view.CustomProgressDialog;
import com.xuqiqiang.uikit.view.ToastMaster;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuqiqiang on 2019/07/12.
 */
public abstract class BaseFragment extends Fragment {
    private final List<Runnable> mRunnablesAfterResume = new ArrayList<>();
    protected Context mContext;
    protected View rootView;
    protected boolean isPaused;
    private boolean refreshWhenResume;
    private boolean isRunning;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return initView(inflater, container, savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
            ApplicationUtils.restartApp(mContext);
        }
        return new View(mContext);
    }

    public View initView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) {
                parent.removeView(rootView);
            }
        } else {
            View view = inflater.inflate(initView(), container, false);
            rootView = view;
            onInitView(view);
            isRunning = true;
            int lazy = lazy();
            if (lazy > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRunning)
                            initData(rootView);
                    }
                }, lazy);
            } else {
                initData(rootView);
            }

        }
        return rootView;
    }

    protected void onInitView(View view) {
    }

    @Override
    public final void onResume() {
        super.onResume();
        isPaused = false;
        if (refreshWhenResume) {
            initData(rootView);
        }
        refreshWhenResume = refreshWhenResume();

        if (checkResumeError()) return;
        try {
            onFragmentResume();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!ArrayUtils.isEmpty(mRunnablesAfterResume)) {
            for (Runnable r : mRunnablesAfterResume) r.run();
            mRunnablesAfterResume.clear();
        }
    }

    protected boolean checkResumeError() {
        return false;
    }

    protected void onFragmentResume() {
    }

    protected void postOnResume(Runnable r) {
        if (isPaused)
            mRunnablesAfterResume.add(r);
        else
            r.run();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void onDestroyView() {
        this.isRunning = false;
        super.onDestroyView();
    }

    protected boolean refreshWhenResume() {
        return false;
    }

    protected boolean hasPresenter() {
        return false;
    }

    protected abstract int initView();

    protected abstract void initData(View view);

    protected void setFragment(int id, Fragment fragment) {
        try {
            FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction mFragmentTransaction = supportFragmentManager.beginTransaction();
            mFragmentTransaction.add(id, fragment).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onShow() {
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    protected int lazy() {
        return 0;
    }

    public void displayProgress(String text) {
        CustomProgressDialog.show(mContext, text, true);
    }

    public void dismissProgress() {
        CustomProgressDialog.close();
    }

    public void showMessage(@NonNull String message) {
        ToastMaster.showToast(mContext, message);
    }
}
