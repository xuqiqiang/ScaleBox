package com.xuqiqiang.scalebox.demo.view.component;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.snailstudio2010.imageviewer.PhotoLoader;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.scalebox.utils.Logger;

import indi.liyi.viewer.scimgv.PhotoView;

/**
 * Created by xuqiqiang on 2019/07/17.
 */
public class GlideImageLoader extends PhotoLoader {

    private OnItemButtonListener onItemButtonChangeListener;

    public void setOnItemButtonListener(OnItemButtonListener listener) {
        this.onItemButtonChangeListener = listener;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void displayImage(final Object src, final int state, final ImageView imageView, final LoadCallback callback) {
        if (src instanceof PhotoEntity) {
            if (((PhotoEntity) src).getType() == PhotoEntity.TYPE_VIDEO) {
                ((PhotoView) imageView).setTouchScaleEnabled(false);
                final ViewGroup button = createButton(imageView);
                ((ImageView) button.getChildAt(0)).setImageResource(R.mipmap.photo_detail_play);
                if (state == 0 || state == 2) {
                    if (onItemButtonChangeListener != null)
                        onItemButtonChangeListener.onItemButtonChange(button);
                }
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.d("_testd_ ivPhoto");
                        if (onItemButtonChangeListener != null)
                            onItemButtonChangeListener.onPlay(button);
                    }
                });
            } else {
                View button = getButton(imageView);
                if (button != null) button.setVisibility(View.GONE);
                if (state == 0 || state == 2) {
                    if (onItemButtonChangeListener != null)
                        onItemButtonChangeListener.onItemButtonChange(null);
                }
                ((PhotoView) imageView).setTouchScaleEnabled(true);
            }
            super.displayImage(((PhotoEntity) src).getFilePath(), state, imageView, callback);
        }
    }

    private ViewGroup getButton(ImageView imageView) {
        ViewGroup viewGroup = (ViewGroup) imageView.getParent();
        Logger.d("_testd_ getChildCount:" + viewGroup.getChildCount());
        ViewGroup ivPhoto = null;
        if (viewGroup.getChildCount() > 1) {
            View view = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
            if (view instanceof PhotoViewButton)
                ivPhoto = (PhotoViewButton) view;
//            ivPhoto = (ImageView) viewGroup.getChildAt(1);
        }
        return ivPhoto;
    }

    private ViewGroup createButton(ImageView imageView) {
        ViewGroup ivPhoto = getButton(imageView);
        if (ivPhoto == null) {
            ivPhoto = new PhotoViewButton(imageView.getContext());
            ImageView iv = new ImageView(imageView.getContext());
            ivPhoto.addView(iv);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);
//                        lp.gravity = Gravity.CENTER;
            ViewGroup viewGroup = (ViewGroup) imageView.getParent();
            viewGroup.addView(ivPhoto, lp);
        }
        return ivPhoto;
    }

    public interface OnItemButtonListener {
        void onItemButtonChange(View button);

        void onPlay(View button);
    }
}
