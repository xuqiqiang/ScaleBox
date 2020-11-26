package com.xuqiqiang.uikit.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class TextViewUtils {

    /**
     * 设置部分文字加下划线, 特殊颜色, 及点击效果
     *
     * @param tv        传入的TextView
     *                  //     * @param strId     传入 TextView 需要展示的文字在string.xml中的id
     * @param underLine 是否显示下划线
     * @param color     部分特殊文字的颜色 传入-1 默认为蓝色
     * @param start     特殊文字从哪个位置开始 传入 -1 则从文字最开始设置
     * @param end       特殊文字从哪个位置结束 传入 -1 则默认设置到文字最后
     * @param textSize  文字大小sp  传入 > 0 的 sp 值
     * @param listener  传入自定义点击监听器 不需要时传 null 即可
     */
    public static void setSpan(TextView tv,
//                               @StringRes int strId,
                               final boolean underLine,
                               final int color, // @ColorRes
                               int start,
                               int end,
                               int textSize,
                               final View.OnClickListener listener) {

        final Context context = tv.getContext();
        tv.setHighlightColor(ContextCompat.getColor(context, android.R.color.transparent));
//        String str = context.getString(strId);
//        SpannableString info = (SpannableString) tv.getText();//new SpannableString(str);
        SpannableString info = (SpannableString) tv.getTag();//new SpannableString(str);
//        CharSequence str = tv.getText();
        if (info == null) {
            info = new SpannableString(tv.getText());
        }

        int startNum = (start <= -1) ? 0 : start;
        int endNum = (end <= -1) ? info.length() : end;

        if (textSize > 0) {
            info.setSpan(new AbsoluteSizeSpan(sp2px(context, textSize)), startNum,
                    endNum, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        info.setSpan(new ClickableSpan() {

                         /**
                          * 重写父类点击事件
                          */
                         @Override
                         public void onClick(View widget) {
                             if (listener != null) {
                                 listener.onClick(widget);
                             }
                         }

                         /**
                          * 重写父类updateDrawState方法  我们可以给TextView设置字体颜色,背景颜色等等...
                          */
                         @Override
                         public void updateDrawState(TextPaint ds) {
                             int colorRes = color;
                             if (color == -1) colorRes = Color.BLUE;
//                             ds.setColor(ContextCompat.getColor(context, colorRes));
                             ds.setColor(colorRes);
                             ds.setUnderlineText(underLine);
                         }
                     }, startNum,
                endNum, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(info);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTag(info);
    }

    public static void setSpan(TextView tv,
                               String text,
                               final boolean underLine,
                               final int color,
                               int textSize,
                               final View.OnClickListener listener) {

        if (tv == null || TextUtils.isEmpty(text)) return;

        final Context context = tv.getContext();
        tv.setHighlightColor(ContextCompat.getColor(context, android.R.color.transparent));
        SpannableString info = (SpannableString) tv.getTag();
        if (info == null) {
            info = new SpannableString(tv.getText());
        }
        String str = info.toString();

        int start = str.indexOf(text);
        int end = start + text.length();

        int startNum = (start <= -1) ? 0 : start;
        int endNum = (end <= -1) ? info.length() : end;

        if (textSize > 0) {
            info.setSpan(new AbsoluteSizeSpan(sp2px(context, textSize)), startNum,
                    endNum, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        info.setSpan(new ClickableSpan() {

                         @Override
                         public void onClick(@NonNull View widget) {
                             if (listener != null) {
                                 listener.onClick(widget);
                             }
                         }

                         /**
                          * 重写父类updateDrawState方法  我们可以给TextView设置字体颜色,背景颜色等等...
                          */
                         @Override
                         public void updateDrawState(@NonNull TextPaint ds) {
                             int colorRes = color;
                             if (color == -1) colorRes = Color.BLUE;
                             ds.setColor(colorRes);
                             ds.setUnderlineText(underLine);
                         }
                     }, startNum,
                endNum, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(info);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTag(info);
    }

    private static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static SpannableString createSpan(String str, Span... spans) {
        SpannableString sStr = new SpannableString(str);
        for (final Span span : spans) {
            sStr.setSpan(new ClickableSpan() {

                             @Override
                             public void onClick(@NonNull View widget) {
                                 Log.d("test", "onClick");
                                 if (span.onClickListener != null)
                                     span.onClickListener.onClick(widget);
                             }

                             @Override
                             public void updateDrawState(@NonNull TextPaint ds) {
                                 ds.setColor(span.color);
                                 ds.setUnderlineText(span.underLine);
                             }
                         }, span.start >= 0 ? span.start : (str.length() + 1 + span.start),
                    span.end >= 0 ? span.end : (str.length() + 1 + span.end),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return sStr;
    }

    /**
     * 设置onClickListener需要调用以下方法:<p>
     * tvTitle.setHighlightColor(0);<p>
     * tvTitle.setMovementMethod(LinkMovementMethod.getInstance());<p>
     * <p>
     * demo:<p>
     * tvTitle.setText(TextViewUtils.createSpan("#" + item.getTitle() + "#",<p>
     * new TextViewUtils.Span(0, 1, 0xFF2257FF),<p>
     * new TextViewUtils.Span(-2, -1, 0xFF2257FF)));
     */
    public static class Span {
        int start;
        int end;
        int color;
        boolean underLine;
        View.OnClickListener onClickListener;

        public Span(int start, int end, int color) {
            this(start, end, color, false, null);
        }

        public Span(int start, int end, int color, boolean underLine, View.OnClickListener listener) {
            this.start = start;
            this.end = end;
            this.color = color;
            this.underLine = underLine;
            this.onClickListener = listener;
        }
    }
}
