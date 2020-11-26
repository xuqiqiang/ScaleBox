package com.xuqiqiang.uikit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.xuqiqiang.uikit.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignKeyWordTextView extends AppCompatTextView {

    //原始文本
    private String mOriginalText = "";
    //关键字
    private String mSignText;
    //关键字颜色
    private int mSignTextColor;

    public SignKeyWordTextView(Context context) {
        super(context);
    }

    public SignKeyWordTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttrs(attrs);
    }

    //初始化自定义属性
    private void initializeAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.SignKeyWordTextView);
        //获取关键字
        mSignText = typedArray.getString(R.styleable.SignKeyWordTextView_signText);
        //获取关键字颜色
        mSignTextColor = typedArray.getColor(R.styleable.SignKeyWordTextView_signTextColor, getTextColors().getDefaultColor());
        typedArray.recycle();
        if (!TextUtils.isEmpty(mSignText)) {
            mOriginalText = getText().toString();
            setSignText(mSignText);
        }
    }

    //重写setText方法
    @Override
    public void setText(CharSequence text, BufferType type) {
        this.mOriginalText = text.toString();
        super.setText(matcherSignText(), type);
    }

    /**
     * 匹配关键字，并返回SpannableStringBuilder对象
     *
     * @return
     */
//    private SpannableStringBuilder matcherSignText() {
//        if (TextUtils.isEmpty(mOriginalText)) {
//            return new SpannableStringBuilder("");
//        }
//        if (TextUtils.isEmpty(mSignText)) {
//            return new SpannableStringBuilder(mOriginalText);
//        }
//        SpannableStringBuilder builder = new SpannableStringBuilder(mOriginalText);
//        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(mSignTextColor);
//        for (int index = 0; index < mSignText.length(); index++) {
//            final char c = mSignText.charAt(index);
//            Pattern p = Pattern.compile(String.valueOf(c));
//            Matcher m = p.matcher(mOriginalText);
//            while (m.find()) {
//                int start = m.start();
//                int end = m.end();
//                builder.setSpan(foregroundColorSpan, start, end,
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//        }
//        return builder;
//    }
    private SpannableStringBuilder matcherSignText() {
        if (TextUtils.isEmpty(mOriginalText)) {
            return new SpannableStringBuilder("");
        }

        if (TextUtils.isEmpty(mSignText)) {
            return new SpannableStringBuilder(mOriginalText);
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(mOriginalText);
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(mSignTextColor);
        final char c = mSignText.charAt(0);
        Pattern p = Pattern.compile(String.valueOf(c));
        Matcher m = p.matcher(mOriginalText);

        while (m.find()) {
            builder.setSpan(foregroundColorSpan, Math.min(m.start(), builder.length()),
                    Math.min(m.start() + mSignText.length(), builder.length()),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            break;
        }

        return builder;
    }

    /**
     * 设置关键字
     *
     * @param signText 关键字
     */
    public void setSignText(String signText) {
        mSignText = signText;
        setText(mOriginalText);
    }

    /**
     * 设置关键字颜色
     *
     * @param signTextColor 关键字颜色
     */
    public void setSignTextColor(int signTextColor) {
        mSignTextColor = signTextColor;
        setText(mOriginalText);
    }
}