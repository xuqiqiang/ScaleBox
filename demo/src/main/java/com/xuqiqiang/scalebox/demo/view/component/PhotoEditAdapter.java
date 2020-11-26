package com.xuqiqiang.scalebox.demo.view.component;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.uikit.view.listener.OnItemClickListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by xuqiqiang on 2020/08/24.
 */
public class PhotoEditAdapter extends RecyclerView.Adapter<PhotoEditAdapter.ViewHolder> {

    private Context context;
    private List<PhotoEditItem> mList;
    private OnItemClickListener mListener;

    public PhotoEditAdapter(Context context, List<PhotoEditItem> list) {
        this.context = context;
        this.mList = list;
    }

    public void setOnItemClickListener(OnItemClickListener onPreviewListener) {
        this.mListener = onPreviewListener;
    }

    @NotNull
    @Override
    public PhotoEditAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
//        int layoutId = R.layout.photo_edit_item;
        View view = LayoutInflater.from(context).inflate(getViewHolderLayoutId(), parent, false);
        return new PhotoEditAdapter.ViewHolder(view);
    }

    protected int getViewHolderLayoutId() {
        return R.layout.photo_edit_item;
    }

    protected int textColorNormal() {
        return Color.WHITE;
    }

    protected int textColorHighlight() {
        return 0xFF0CAC74;
    }

    @Override
    public void onBindViewHolder(@NotNull PhotoEditAdapter.ViewHolder holder, int position) {
        if (mList.get(position).getIcon() > 0) {
            holder.ivImage.setImageResource(mList.get(position).getIcon());
            holder.ivImage.setVisibility(View.VISIBLE);
        } else {
            holder.ivImage.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(mList.get(position).getText())) {
            holder.tvText.setText(mList.get(position).getText());
            holder.tvText.setTextColor(mList.get(position).isOn() ? textColorHighlight() : textColorNormal());
            holder.tvText.setVisibility(View.VISIBLE);
        } else {
            holder.tvText.setVisibility(View.GONE);
        }
        if (mList.get(position).isDisabled()) {
            ((ViewGroup) holder.itemView).getChildAt(0).setAlpha(0.2f);
        } else {
            if (((ViewGroup) holder.itemView).getChildAt(0).getAlpha() < 1f)
                ((ViewGroup) holder.itemView).getChildAt(0).setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView ivImage;
        TextView tvText;

        ViewHolder(final View view) {
            super(view);
            ivImage = view.findViewById(R.id.iv_image);
            tvText = view.findViewById(R.id.tv_text);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onItemClick(itemView, getAdapterPosition());
        }
    }
}