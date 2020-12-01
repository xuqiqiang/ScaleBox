package com.xuqiqiang.uikit.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ActivityUtils {

    public static void startFade(Activity fromActivity, Intent intent) {
        fromActivity.startActivity(intent);
        //context.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        //context.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        //fromActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
        fromActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void startForResultFade(Activity fromActivity, Intent intent, int requestCode) {
        fromActivity.startActivityForResult(intent, requestCode);
        //context.overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        //context.overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        //fromActivity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
        fromActivity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static boolean finishFade(Activity activity) {
        activity.finish();
        //activity.overridePendingTransition(R.anim.left_in, R.anim.right_out);
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        return true;
    }

    public static void setFullScreen(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        window.setAttributes(params);
    }

    public static void recreate(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            activity.recreate();
        } else {
            Intent intent = activity
                    .getIntent();
            activity.overridePendingTransition(
                    0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.finish();
            activity.overridePendingTransition(
                    0, 0);
            activity.startActivity(intent);
        }
    }
}