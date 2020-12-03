package com.xuqiqiang.scalebox.demo.view.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.xuqiqiang.scalebox.GalleryBoxAdapter;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoWrapper;
import com.xuqiqiang.scalebox.demo.view.component.GalleryPhotoViewer;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.utils.IntentUtils;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends GalleryBoxAdapter<PhotoEntity, PhotoWrapper> {

    private int[] mSpanCounts;
    private GalleryPhotoViewer mPhotoViewer;

    public GalleryAdapter(Context context, List<PhotoEntity> photoList) {
        super(context, photoList);
        setRVBackgroundColor(0xFF1B1E1E);
    }

    public void remove(PhotoEntity photoEntity) {
        for (int i = 0; i < rvPhotos.length; i++) {
            mPhotoLists[i].remove(photoEntity);
            rvPhotos[i].getAdapter().notifyDataSetChanged();
        }
    }

    public View getPhotoPreviewPhoto(int position) {
        return ((BaseQuickAdapter) rvPhotos[getLevel()].getAdapter()).getViewByPosition(
                toPreviewIndex(getLevel(), position), R.id.iv_photo);
    }

    public void setPhotoViewer(GalleryPhotoViewer photoViewer) {
        this.mPhotoViewer = photoViewer;

        mPhotoViewer.setOnItemChangedListener(new GalleryPhotoViewer.OnItemChangedListener() {
            @Override
            public View onItemChanged(int position) {
                return getPhotoPreviewView(position
                        + ((PhotoAdapter) rvPhotos[getLevel()].getAdapter()).getOffsetCount());
            }

            @Override
            public void onItemDeleted(final int position) {
                PhotoEntity photoEntity = mPhotoList.remove(position);
//                synchronized (mAllPhotoLock) {
//                    mAllPhotoList.remove(photoEntity);
//                }
                remove(photoEntity);

                if (ArrayUtils.isEmpty(mPhotoList)) {
                    mPhotoViewer.hide();
                } else {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            int availableIndex = position;
                            if (availableIndex >= mPhotoList.size())
                                availableIndex = mPhotoList.size() - 1;
                            mPhotoViewer.show(mPhotoList, availableIndex,
                                    getPhotoPreviewPhoto(availableIndex));
                        }
                    }, 100);
                }
            }
        });
//        mPhotoViewer.setOnPhotoViewerListener(open -> {
//            setStatusBarDarkTheme(!open);
//            setColorTitle(open ? 0xFF1B1E1E : Color.WHITE);
//        });
    }

    @Override
    protected int[] initSpanCounts() {
        return new int[]{1, 3, 5, 9};//, 15};
    }

    @Override
    protected void onInitSpan(int[] spanCounts) {
        mSpanCounts = spanCounts;
        super.onInitSpan(spanCounts);
    }

//    @Override
//    protected void onRecyclerViewScrollStateChanged(int level, @NonNull RecyclerView recyclerView, int newState) {
//        if (level >= 4) {
//            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                Fresco.getImagePipeline().resume();
//            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                Fresco.getImagePipeline().pause();
//            }
//        }
//    }

    @Override
    @SuppressWarnings("rawtypes")
    protected RecyclerView.Adapter initPhotoAdapter(final List<PhotoWrapper> list, int spanSize) {
        PhotoAdapter adapter = new PhotoAdapter(list, spanSize);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                PhotoWrapper wrapper = list.get(position);
                if (wrapper.isPhoto()) {
                    if (mPhotoViewer != null) {
                        mPhotoViewer.show(mPhotoList, position - ((PhotoAdapter) adapter).getOffsetCount(),
                                ((ViewGroup) view).getChildAt(0));
                    } else {
                        IntentUtils.goToGallery(mContext,
                                Uri.fromFile(new File(wrapper.getPhotoEntity().getFilePath())));
                    }
                }
            }
        });
        return adapter;
    }

//    @Override
//    protected void loadImage(RecyclerView.ViewHolder viewHolder, int level, int realPosition) {
//        if (!(viewHolder instanceof BaseViewHolder)) return;
//        ImageView imageView = ((BaseViewHolder) viewHolder).getView(R.id.iv_photo);
//        if (imageView.getTag(R.id.tag_image_loaded) instanceof Boolean) return;
//        PhotoEntity item = mPhotoList.get(realPosition);
////        if (item instanceof QuickPhotoEntity && ((QuickPhotoEntity) item).getPhotoEntity() != null) {
//        String filePath = item.getDisplayFilePath();
//        int size = (int) (ScreenUtils.getWidth() / (float) mSpanCounts[level]);
//        RxImageUtils.loadBitmap(mContext, filePath,
//                size, size, bitmap -> {
//                    if (imageView.getTag(R.id.tag_image_loaded) instanceof Boolean) return;
//                    if (bitmap != null) {
//                        imageView.setImageBitmap(bitmap);
//                        imageView.setTag(new int[]{bitmap.getWidth(), bitmap.getHeight()});
//                    }
//                });
////        }
//    }

    @Override
    protected PhotoWrapper toPhotoWrapper(PhotoEntity entity) {
        return new PhotoWrapper(entity);
    }

    @Override
    protected PhotoWrapper toPhotoWrapper(String date) {
        return new PhotoWrapper(date);
    }
}
