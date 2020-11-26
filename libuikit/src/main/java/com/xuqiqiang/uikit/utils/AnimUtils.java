package com.xuqiqiang.uikit.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import com.xuqiqiang.uikit.R;

import static android.animation.PropertyValuesHolder.ofFloat;

public class AnimUtils {
    public static final int ANIM_DURING = 300;

    public static void show(View view) {
        show(view, null);
    }

    public static void show(View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                ofFloat("alpha", 1f));
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

    public static void hide(View view) {
        hide(view, null);
    }

    public static void hide(final View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                ofFloat("alpha", 0f));
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

    public static void showScale(View view) {
        showScale(view, null);
    }

    public static void showScale(View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                ofFloat("scaleX", 1f),
                ofFloat("scaleY", 1f),
                ofFloat("alpha", 1f));
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

    public static void hideScale(View view) {
        hideScale(view, null);
    }

    public static void hideScale(final View view, final Runnable onComplete) {
        if (view.getTag(R.id.tag_anim_show) != null && view.getTag(R.id.tag_anim_show) instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) view.getTag(R.id.tag_anim_show);
            if (objectAnimator.isRunning()) objectAnimator.cancel();
        }

        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view,
                ofFloat("scaleX", 0.7f),
                ofFloat("scaleY", 0.7f),
                ofFloat("alpha", 0f));
        objectAnimator.setDuration(ANIM_DURING);
        objectAnimator.start();
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
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
