package com.xuqiqiang.uikit.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xuqiqiang.uikit.R;

import java.lang.ref.WeakReference;

public class CustomProgressDialog extends Dialog implements DialogInterface.OnCancelListener {

    private volatile static CustomProgressDialog sDialog;
    // for test
    private static int progress;
    private WeakReference<Context> mContextRef = new WeakReference<>(null);
    private CharSequence mText;
    private boolean mIndeterminate = true;
    private ProgressBar pbLoading;
    private RoundProgressBar rpbLoading;

    private CustomProgressDialog(Context context, CharSequence message) {
        super(context, R.style.CustomProgressDialog);

        mContextRef = new WeakReference<>(context);

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_progress, null);
        if (!TextUtils.isEmpty(message)) {
            mText = message;
//            View llContent = view.findViewById(R.id.ll_content);
//            ViewUtils.setSize(llContent, (int) DisplayUtils.dip2px(context, 200), ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView tvMessage = view.findViewById(R.id.tv_message);
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(message);
        }
        pbLoading = view.findViewById(R.id.pb_loading);
        rpbLoading = view.findViewById(R.id.rpb_loading);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(view, lp);
        setOnCancelListener(this);
    }

    public static void show(Context context) {
        show(context, null);
    }

    public static void show(Context context, CharSequence message) {
        show(context, message, true);
    }

    public static void show(Context context, CharSequence message, boolean cancelable) {
        if (sDialog != null && sDialog.isShowing()) {
            if (TextUtils.equals(sDialog.mText, message)) return;
            // Fix: java.lang.IllegalArgumentException: View=DecorView not attached to window manager
            try {
                sDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!(context instanceof Activity)) {
            return;
        }

        sDialog = new CustomProgressDialog(context, message);
        sDialog.setCancelable(cancelable);

        if (sDialog != null && !sDialog.isShowing() && !((Activity) context).isFinishing()) {
            try {
                sDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setProgress(int progress) {
        if (sDialog != null && sDialog.isShowing()) {
            sDialog.setLoadingProgress(progress);
        }
    }

    public static void close() {
        if (sDialog != null && sDialog.isShowing()) {
            // Fix: java.lang.IllegalArgumentException: View=DecorView not attached to window manager
            try {
                sDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sDialog = null;
    }

    @Deprecated
    public static void stop() {
        close();
    }

    public static void main(Context context) {
        CustomProgressDialog.show(context, "加载中...", true);
        progress = 0;
        CustomProgressDialog.setProgress(progress);
        testCase(context);
    }

    private static void testCase(final Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progress += 1;
                CustomProgressDialog.setProgress(progress);
                if (progress >= 100) {
                    CustomProgressDialog.close();
                    ToastMaster.showToast(context, "加载完成");
                    return;
                }
                testCase(context);
            }
        }, 30);
    }

    private void setLoadingProgress(int progress) {
        if (mIndeterminate) {
            mIndeterminate = false;
            pbLoading.setVisibility(View.GONE);
            rpbLoading.setVisibility(View.VISIBLE);
        }
        rpbLoading.setProgress(progress);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
//        Context context = mContextRef.get();
//        if (context != null) {
//            Toast.makeText(context, "cancel", Toast.LENGTH_SHORT).show();
//        }
    }
}
