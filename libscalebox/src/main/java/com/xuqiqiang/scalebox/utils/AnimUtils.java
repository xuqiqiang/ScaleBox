package com.xuqiqiang.scalebox.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;

import com.xuqiqiang.scalebox.R;

public class AnimUtils {
    private static final int ANIM_DURING = 300;

    public static void showView(View view) {
        showView(view, null);
    }

    public static void showView(View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

//        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 0.7f, 1f);
//        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 0.7f, 1f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
        objectAnimator.setDuration(ANIM_DURING);
        objectAnimator.start();
        if (onComplete != null) {
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onComplete.run();
                }
            });
        }

        view.setVisibility(View.VISIBLE);
        view.setTag(R.id.tag_anim_show, objectAnimator);
    }

    public static void hideView(View view) {
        hideView(view, null);
    }

    public static void hideView(final View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

//        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 0.7f);
//        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 0.7f);
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 0f);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, alpha);
        objectAnimator.setDuration(ANIM_DURING);
        objectAnimator.start();
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.INVISIBLE);
                if (onComplete != null) onComplete.run();
            }
        });
        view.setTag(R.id.tag_anim_show, objectAnimator);
    }


    public static void setAlpha(View view, float alpha, final Runnable... onComplete) {
        if (view.getTag(R.id.tag_anim_alpha) != null && view.getTag(R.id.tag_anim_alpha) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_alpha);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", alpha);
        objectAnimator.setDuration(ANIM_DURING);
        objectAnimator.start();
        if (onComplete != null) {
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    for (Runnable r : onComplete)
                        r.run();
                }
            });
        }
        view.setTag(R.id.tag_anim_alpha, objectAnimator);
    }
}
