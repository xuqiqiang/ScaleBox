package com.xuqiqiang.scalebox.demo.view.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.Album;
import com.xuqiqiang.scalebox.demo.utils.FrescoImageLoader;
import com.xuqiqiang.scalebox.demo.utils.RecyclerViewCursorAdapter;
import com.xuqiqiang.scalebox.utils.Utils;

/**
 * Created by xuqiqiang on 2020/11/12.
 */
public class AlbumAdapter extends RecyclerViewCursorAdapter<AlbumAdapter.AlbumViewHolder> {

    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public AlbumAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_list_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder holder, Cursor cursor) {
        Album album = Album.valueOf(cursor);
        holder.tvAlbumTitle.setText(album.getDisplayName());
        holder.tvPhotoCount.setText(mContext.getResources().getString(R.string.bracket_num,
                album.getCount()));
        int size = (int) Utils.dip2px(mContext, 32);
        FrescoImageLoader.getInstance().bindImage(holder.ivAlbumCover,
                album.buildCoverUri(), size, size);
        holder.itemView.setTag(album);
    }

    @Override
    protected void onContentChanged() {
    }

    public interface OnItemClickListener {
        void onItemClick(Album album, int position);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvAlbumTitle;
        TextView tvPhotoCount;
        ImageView ivAlbumCover;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvAlbumTitle = itemView.findViewById(R.id.tv_album_name);
            tvPhotoCount = itemView.findViewById(R.id.tv_photo_count);
            ivAlbumCover = itemView.findViewById(R.id.iv_album_cover);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick((Album) itemView.getTag(), getAdapterPosition());
            }
        }
    }
}