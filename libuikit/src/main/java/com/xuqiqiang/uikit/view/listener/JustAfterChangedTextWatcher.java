package com.xuqiqiang.uikit.view.listener;

import android.text.TextWatcher;

/**
 * Created by xuqiqiang on 2020/5/22.
 */
public abstract class JustAfterChangedTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
