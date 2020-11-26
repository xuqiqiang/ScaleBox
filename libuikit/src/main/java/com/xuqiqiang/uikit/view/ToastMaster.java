package com.xuqiqiang.uikit.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.xuqiqiang.uikit.R;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;

/**
 * Created by xuqiqiang on 2016/05/17.
 */
public class ToastMaster {

    private static SoftReference<Toast> sToast;

    private ToastMaster() {
    }

    public static Toast showToast(Context context, CharSequence text) {
        return showToast(context, text, Toast.LENGTH_SHORT, true);
    }

    public static Toast showToast(Context context, int textId) {
        return showToast(context, context.getString(textId));
    }

    public static Toast showToastLong(Context context, CharSequence text) {
        return showToast(context, text, Toast.LENGTH_LONG, true);
    }

    public static Toast showToastLong(Context context, int textId) {
        return showToastLong(context, context.getString(textId));
    }

    public static Toast showToastLowPriority(Context context, CharSequence text) {
        return showToast(context, text, Toast.LENGTH_SHORT, false);
    }

    public static Toast showToastLowPriority(Context context, int textId) {
        return showToastLowPriority(context, context.getString(textId));
    }

    public static Toast showToast(Context context, CharSequence text, int during, boolean highPriority) {
        Toast toast = new Toast(context);
        toast.setDuration(during);
        View view = getView(context, text);
        setContextCompat(view, toast);
        toast.setView(view);
        if (highPriority) setToast(toast);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        return toast;
    }

    public static void setToast(Toast toast) {
        if (sToast != null) {
            Toast t = sToast.get();
            if (t != null) {
                t.cancel();
            }
        }
        if (toast == null) sToast = null;
        else sToast = new SoftReference<>(toast);
    }

    public static void cancelToast() {
        setToast(null);
    }

    private static View getView(Context context, CharSequence text) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast, null);
        TextView tv_text = view.findViewById(R.id.text);
        tv_text.setText(text);
        return view;
    }

    // Fix bug: Fatal Exception: android.view.WindowManager$BadTokenException:
    // Unable to add window -- token android.os.BinderProxy@1c4411f is not valid; is your activity running?
    private static void setContextCompat(@NonNull View view, @NonNull Toast toast) {
        if (Build.VERSION.SDK_INT == 25) {
            try {
                Field field = View.class.getDeclaredField("mContext");
                field.setAccessible(true);
                field.set(view, new SafeToastContext(view.getContext(), toast));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public void setBadTokenListener(Toast toast, @NonNull BadTokenListener listener) {
        final Context context = toast.getView().getContext();
        if (context instanceof SafeToastContext) {
            ((SafeToastContext) context).setBadTokenListener(listener);
        }
    }

    public interface BadTokenListener {
        void onBadTokenCaught(@NonNull Toast toast);
    }

    private static class SafeToastContext extends ContextWrapper {

        @NonNull
        private Toast toast;

        @Nullable
        private BadTokenListener badTokenListener;

        private SafeToastContext(@NonNull Context base, @NonNull Toast toast) {
            super(base);
            this.toast = toast;
        }

        @Override
        public Context getApplicationContext() {
            return new ApplicationContextWrapper(getBaseContext().getApplicationContext());
        }

        public void setBadTokenListener(@NonNull BadTokenListener badTokenListener) {
            this.badTokenListener = badTokenListener;
        }

        private final class ApplicationContextWrapper extends ContextWrapper {

            private ApplicationContextWrapper(@NonNull Context base) {
                super(base);
            }

            @Override
            public Object getSystemService(@NonNull String name) {
                if (Context.WINDOW_SERVICE.equals(name)) {
                    // noinspection ConstantConditions
                    return new WindowManagerWrapper((WindowManager) getBaseContext().getSystemService(name));
                }
                return super.getSystemService(name);
            }
        }

        private final class WindowManagerWrapper implements WindowManager {

            private static final String TAG = "WindowManagerWrapper";
            @NonNull
            private final WindowManager base;

            private WindowManagerWrapper(@NonNull WindowManager base) {
                this.base = base;
            }

            @Override
            public Display getDefaultDisplay() {
                return base.getDefaultDisplay();
            }

            @Override
            public void removeViewImmediate(View view) {
                base.removeViewImmediate(view);
            }

            @Override
            public void addView(View view, ViewGroup.LayoutParams params) {
                try {
                    Log.d(TAG, "WindowManager's addView(view, params) has been hooked.");
                    base.addView(view, params);
                } catch (BadTokenException e) {
                    Log.i(TAG, e.getMessage());
                    if (badTokenListener != null) {
                        badTokenListener.onBadTokenCaught(toast);
                    }
                } catch (Throwable throwable) {
                    Log.e(TAG, "[addView]", throwable);
                }
            }

            @Override
            public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
                base.updateViewLayout(view, params);
            }

            @Override
            public void removeView(View view) {
                base.removeView(view);
            }
        }
    }
}
