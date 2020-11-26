package com.xuqiqiang.scalebox.demo.view.component;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.utils.BitmapUtils;
import com.xuqiqiang.uikit.utils.DisplayUtils;
import com.snailstudio2010.imageviewer.ImageInfo;
import com.snailstudio2010.imageviewer.ImageUtils;
import com.snailstudio2010.imageviewer.ImageViewerHelper;
import com.xuqiqiang.uikit.utils.MimeUtils;
import com.xuqiqiang.uikit.utils.ScreenUtils;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.uikit.utils.TimeUtils;
import com.xuqiqiang.scalebox.utils.Logger;
import com.xuqiqiang.uikit.utils.IntentUtils;
import com.xuqiqiang.uikit.utils.ViewUtils;
import com.xuqiqiang.uikit.view.dialog.BaseDialog;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import indi.liyi.viewer.ImageDrawee;
import indi.liyi.viewer.ImageTransfer;
import indi.liyi.viewer.ImageViewer;
import indi.liyi.viewer.ViewData;
import indi.liyi.viewer.ViewerStatus;
import indi.liyi.viewer.listener.OnBrowseStatusListener;
import indi.liyi.viewer.listener.OnItemClickListener;

import static com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity.TYPE_IMAGE;
import static com.xuqiqiang.uikit.utils.StringUtils.getFormatSize;

/**
 * Created by xuqiqiang on 2020/07/22.
 */
public class PhotoViewer extends FrameLayout {

    protected Activity mContext;
    protected List<PhotoEntity> mPhotoAddressList;
    protected OnItemChangedListener mOnItemChangedListener;
    protected ImageViewer mImageViewer;
    private View llInfo;
    private RecyclerView rvEdit;
    private TextView tvTitle;
    private View flDetail;
    private NestedScrollView svDetail;
    private TextView tvFileName;
    private TextView tvFileTime;
    private TextView tvFileSize;
    private TextView tvFileType;
    private TextView tvFileDevice;
    private View flFileSize;
    private View titlePhotoViewer;
    private ImageInfo mImageInfo;
    private boolean isImageShowing;
    private List<PhotoEditItem> mOptionList;
    private PhotoEditAdapter mOptionAdapter;
    private OnPhotoViewerListener mOnPhotoViewerListener;

    private ImageViewerHelper mImageViewerHelper;
    private GlideImageLoader mImageLoader;
    private View mItemButton;
    private float mDownX;
    private float mDownY;
    private float mMoveY;
    private long mDownTime;
    private long mMoveTime;
    private int mState = 0;
    private boolean isCollapsing;

    public PhotoViewer(Context context) {
        this(context, null, 0);
    }

    public PhotoViewer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = (Activity) context;
        ScreenUtils.initialize(mContext);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.photo_viewer, this);
        initView(view);
        mImageViewer.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(int position, ImageView imageView) {
                llInfo.setVisibility(llInfo.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                return true;
            }
        });
        mImageViewer.setOnBrowseStatusListener(new OnBrowseStatusListener() {
            @Override
            public void onBrowseStatus(int status) {
                Logger.d("_StoryImage_ onBrowseStatus:" + status);
                if (status == ViewerStatus.STATUS_SILENCE) {
                    isImageShowing = false;
                    setVisibility(View.INVISIBLE);
                    mImageViewer.getBackground().setAlpha(255);
                    if (mOnPhotoViewerListener != null)
                        mOnPhotoViewerListener.onStateChanged(false);
//                RecommendUtils.notifyChanged();
                } else if (status == ViewerStatus.STATUS_BEGIN_OPEN) {
                    setVisibility(View.VISIBLE);
                    if (mOnPhotoViewerListener != null) mOnPhotoViewerListener.onStateChanged(true);
                }
            }
        });
        mImageViewer.setOnTransListener(new ImageTransfer.OnTransCallback() {
            @Override
            public void onStart() {
            }

            @Override
            public void onRunning(float progress) {
                llInfo.setAlpha(progress);
                if (isCollapsing)
                    flDetail.setAlpha(progress);
                Logger.d("onRunning:" + (mItemButton != null) + "," + progress);
                if (mItemButton != null)
                    mItemButton.setAlpha(progress * progress);
            }

            @Override
            public void onEnd() {
            }
        });

        mImageViewer.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Logger.d("onPageSelected:" + position);
                PhotoEntity photoEntity = mPhotoAddressList.get(position);
                View v = mOnItemChangedListener.onItemChanged(position);
                if (v != null) {
                    List<ViewData> list = mImageViewer.getViewData();
                    ViewData data = list.get(position);
                    int[] pos = new int[2];
                    v.getLocationOnScreen(pos);
                    data.setTargetX(pos[0]);
                    data.setTargetY(pos[1]);
                    data.setTargetWidth(v.getWidth());
                    data.setTargetHeight(v.getHeight() + 1);
                }

                refreshPhotoInfo(photoEntity);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Logger.d("onPageScrollStateChanged:" + state);
            }
        });


        initOption();

        mImageViewerHelper = new ImageViewerHelper(mContext);
        mImageLoader = new GlideImageLoader();
        mImageLoader.setOnItemButtonListener(new GlideImageLoader.OnItemButtonListener() {
            @Override
            public void onItemButtonChange(View button) {
                mItemButton = button;
            }

            @Override
            public void onPlay(View button) {
                // for test
//                VideoActivity.start(mContext, mPhotoAddressList.get(mImageViewer.getCurrentPosition()).getFilePath());
            }
        });

        view.findViewById(R.id.bt_photo_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        post(new Runnable() {
            @Override
            public void run() {
                LayoutParams lp = (LayoutParams) flDetail.getLayoutParams();
                lp.height = (int) (getHeight() - DisplayUtils.dip2px(mContext, 220));
                lp.setMargins(0, getHeight(), 0, 0);
                flDetail.setLayoutParams(lp);
            }
        });
    }

    private void initView(View view) {
        mImageViewer = view.findViewById(R.id.image_viewer);
        llInfo = view.findViewById(R.id.ll_info);
        tvTitle = view.findViewById(R.id.tv_title);
        rvEdit = view.findViewById(R.id.rv_edit);
        flDetail = view.findViewById(R.id.fl_detail);
        svDetail = view.findViewById(R.id.sv_detail);
        tvFileName = view.findViewById(R.id.tv_file_name);
        tvFileTime = view.findViewById(R.id.tv_file_time);
        tvFileSize = view.findViewById(R.id.tv_file_size);
        tvFileType = view.findViewById(R.id.tv_file_type);
        tvFileDevice = view.findViewById(R.id.tv_file_device);
        flFileSize = view.findViewById(R.id.fl_file_size);
        titlePhotoViewer = view.findViewById(R.id.title_photo_viewer);
    }

    private void refreshPhotoInfo(PhotoEntity photoEntity) {
        initFileInfo(photoEntity);
        if (mOptionList != null) {
            if (mOptionList.get(3).isDisabled() == (photoEntity.getType() == TYPE_IMAGE)) {
                mOptionList.get(3).setDisabled(photoEntity.getType() != TYPE_IMAGE);
                mOptionAdapter.notifyItemChanged(3);
            }
        }
    }

    private void initFileInfo(PhotoEntity photoEntity) {

        File file = new File(photoEntity.getFilePath());
        String strTime = TimeUtils.formatTime(file.lastModified(), "MM月dd日 HH:mm");
        tvTitle.setText(strTime);

        tvFileName.setText(file.getName());

        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTimeInMillis(file.lastModified());
        String[] days = new String[]{"-", "日", "一", "二", "三", "四", "五", "六"};
        String[] times = strTime.split(" ");
        strTime = times[0] + " 星期" + days[calendar.get(Calendar.DAY_OF_WEEK)] + " " + times[1];
        tvFileTime.setText(strTime);


        flFileSize.setVisibility(View.VISIBLE);
        String strSize = "---";
        if (photoEntity.getType() == TYPE_IMAGE) {
            Point size = BitmapUtils.getBitmapSize(photoEntity.getFilePath());
            strSize = size.x + "x" + size.y + " " + getFormatSize(file.length());
        } else {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(photoEntity.getFilePath());
                String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); //宽
                String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); //高
                String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);//视频的方向角度
//                  long duration = Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000;//视频的长度
                int orientation = 0;
                try {
                    orientation = Integer.parseInt(rotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                strSize = (orientation % 180 == 0 ? (width + "x" + height) : (height + "x" + width))
                        + " " + getFormatSize(file.length());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tvFileSize.setText(strSize);
        tvFileType.setText(photoEntity.getType() == TYPE_IMAGE ? "照片" : "视频");
        String strDevice = "";

        try {
            ExifInterface exifInterface = new ExifInterface(photoEntity.getFilePath());
            String fMake = exifInterface
                    .getAttribute(ExifInterface.TAG_MAKE);
            if (!TextUtils.isEmpty(fMake)) strDevice += fMake + " ";
            String fModel = exifInterface
                    .getAttribute(ExifInterface.TAG_MODEL);
            if (!TextUtils.isEmpty(fModel)) strDevice += fModel;
        } catch (IOException e) {
            e.printStackTrace();
        }
        tvFileDevice.setText(strDevice);
    }

    protected void initOption() {

//        RecyclerView rvEdit = new RecyclerView(mContext);

        GridLayoutManager manager = new GridLayoutManager(mContext, 5);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        rvEdit.setLayoutManager(manager);

        mOptionList = ArrayUtils.createList(
                new PhotoEditItem(R.mipmap.photo_share, "分享"),
                new PhotoEditItem(R.mipmap.photo_delete, "删除"),
                new PhotoEditItem(R.mipmap.photo_share, "导出"),
                new PhotoEditItem(R.mipmap.photo_edit, "编辑"),
                new PhotoEditItem(R.mipmap.photo_more, "更多")
        );
        mOptionAdapter = new PhotoEditAdapter(mContext, mOptionList);
        // for test
        mOptionAdapter.setOnItemClickListener(new com.xuqiqiang.uikit.view.listener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                PhotoEntity photoEntity = mPhotoAddressList.get(mImageViewer.getCurrentPosition());
                if (position == 0) {
                    if (photoEntity.getType() == PhotoEntity.TYPE_IMAGE) {
                        IntentUtils.sharePic(mContext, photoEntity.getFilePath());
                    } else if (photoEntity.getType() == PhotoEntity.TYPE_VIDEO) {
                        IntentUtils.shareVideo(mContext, photoEntity.getFilePath());
                    }
//                Utils.sharePhotos(mContext, ArrayUtils.createList(photoEntity));
                } else if (position == 1) {
                    new BaseDialog.Builder(mContext)
                            .setTitle("删除")
                            .setMessage("是否删除此图片")
                            .setPositiveButton("删除", 0xFFD33636, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //                            File file = new File(photoEntity.getFilePath());
//                            boolean result;
//                            if (isPhotoSet) {
//                                result = FileUtils.delete(photoEntity.getFilePath());
//                            } else {
//                                String timeStamp = TimeUtils.formatTime(
//                                        System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L, "yyyyMMdd");
//                                String path = Cache.createRealFilePath(Constants.DIR_RECYCLE) + File.separator + timeStamp;
//                                if (photoEntity.getType() == TYPE_SET) {
//                                    result = FileUtils.moveDir(file,
//                                            new File(path, file.getName()));
//                                } else {
//                                    result = FileUtils.moveFile(file,
//                                            new File(path, file.getName()));
//                                }
//                            }
//                            if (result) {
//                                mOnItemChangedListener.onItemDeleted(mImageViewer.getCurrentPosition());
//                            } else {
//                                ToastMaster.showToast(mContext, "无法删除");
//                            }
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create().show();
                } else if (position == 2) {
//                BaseDialog.show(mContext, "导出", "是否将图片导出至本地相册", dialog -> {
//                    observable(() -> Utils.exportPhoto(photoEntity),
//                            mContext, "正在导入到相册")
//                            .subscribe(new HandleSubscriber<Boolean>() {
//                                @Override
//                                public void onNext(Boolean result) {
//                                    mContext.showMessage(result ? "已导入到相册" : "无法导入到相册");
//                                }
//                            });
//                    return true;
//                }, null);
                } else if (position == 3) {
//                if (photoEntity.getType() != TYPE_SET) {
//                    String targetDirPath;
//                    if (isPhotoSet) {
//                        File file = new File(photoEntity.getFilePath());
//                        targetDirPath = file.getParent();
//                    } else {
//                        targetDirPath = Cache.getRealFilePath(Constants.DIR_CAMERA);
//                    }
//                    EditPhotoActivity.start(mContext, photoEntity.getFilePath(),
//                            targetDirPath, Constants.RequestCode.PHOTO_EDIT);
//                }
                } else if (position == 4) {
//                if (isPhotoSet) {
//                    File file = new File(photoEntity.getFilePath());
//                    PhotoSetActivity.start(mContext, file.getParent(), Constants.RequestCode.PHOTO_EDIT);
//                } else {
//                    vpEdit.setCurrentItem(1);
//                }
                }
            }
        });
        rvEdit.setAdapter(mOptionAdapter);
    }

    public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
        this.mOnItemChangedListener = onItemChangedListener;
    }

    public void setOnPhotoViewerListener(OnPhotoViewerListener onPhotoViewerListener) {
        this.mOnPhotoViewerListener = onPhotoViewerListener;
    }

    public void show(final List<PhotoEntity> photoAddressList, final int index, final View view) {
        if (index < 0 || index >= photoAddressList.size()) return;
        mPhotoAddressList = photoAddressList;
        final PhotoEntity[] arr = photoAddressList.toArray(new PhotoEntity[0]);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mImageInfo = mImageViewerHelper.initImageInfo(arr, index, view);
                mImageInfo.setShowIndex(false);
                int[] size = null;
                if (view != null) {
                    size = (int[]) view.getTag();
                } else {
                    for (ImageInfo.Info info : mImageInfo.getList()) {
                        Point s = BitmapUtils.getBitmapSize(((PhotoEntity) info.getSrc()).getFilePath());
                        info.setWidth(DisplayUtils.dip2px(mContext, 100));
                        info.setHeight(info.getWidth() * s.y / (float) s.x);
                        info.setX((ScreenUtils.getWidth() - info.getWidth()) / 2);
                        info.setY(ScreenUtils.getHeight());
                    }
                }
                Logger.d("_test_i_ size 1:" + Arrays.toString(size));
                if (size == null || size[0] == size[1]) {
                    String filePath = photoAddressList.get(index).getFilePath();
                    if (MimeUtils.isImage(filePath)) {
                        Point s = BitmapUtils.getBitmapSize(filePath);
                        size = new int[]{s.x, s.y};
                        if (view != null) view.setTag(size);
                    }
                }
                Logger.d("_test_i_ size 2:" + Arrays.toString(size));
                if (size == null) size = new int[]{0, 0};

//                if(view instanceof DraweeView) {
//                    long start = System.currentTimeMillis();
//                    DraweeView draweeView = (DraweeView) view;
////                    Bitmap bmp = ((BitmapDrawable)((RootDrawable)draweeView.getTopLevelDrawable()).getDrawable()).getBitmap();
//
//
//
//                    Bitmap bmp = ViewUtils.getViewBitmap(draweeView, size[0], size[1]);
//                    Logger.d("_test_i_ setCachedBitmap:" + (bmp.getWidth()) + " " + bmp.getHeight() + " " + (System.currentTimeMillis() - start));
//                    mImageLoader.setCachedBitmap(bmp);
//                }

                if (view != null) {
                    long start = System.currentTimeMillis();
                    Bitmap bmp = ViewUtils.getViewBitmap(view);//, view.getWidth(), size[1] * view.getWidth() / size[0]);
                    Logger.d("_test_i_ getDrawingCache:" + (bmp.getWidth()) + " " + bmp.getHeight() + " " + (System.currentTimeMillis() - start));
                    mImageLoader.setCachedBitmap(bmp);
                }
                isImageShowing = ImageUtils.showImage(mContext, mImageViewer, mImageInfo,
                        size[0], size[1], mImageLoader);
                setVisibility(View.VISIBLE);
            }
        };
        if (view != null) view.post(r);
        else r.run();
        llInfo.setVisibility(View.VISIBLE);
    }

    public boolean isImageShowing() {
        return isImageShowing;
    }

    public int getCurrentPosition() {
        return mImageViewer.getCurrentPosition();
    }

    public List<PhotoEntity> getPhotoList() {
        return mPhotoAddressList;
    }

    //    @OnClick(R.id.bt_photo_back)
    public void hide() {
        mImageViewer.cancel();
    }

    public void onResume() {
        mImageViewer.onResume();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Logger.d("_test_t_ dispatchTouchEvent:" + ev.getAction() + "," + ev.getY());
        ImageDrawee drawee = mImageViewer.getCurrentItem();
        if (drawee != null && drawee.getScale() > 1.0f) return super.dispatchTouchEvent(ev);
        Logger.d("_test_t_ getScale:" + (drawee == null ? 0 : drawee.getScale()));
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDownX = ev.getX();
            mDownY = ev.getY();
            if (mDownY > DisplayUtils.dip2px(mContext, 220) && svDetail.canScrollVertically(-1)) {
                mState = 2;
            } else {
                mState = 0;
                mDownTime = System.currentTimeMillis();
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float x = ev.getX();
            float y = ev.getY();
            if (mState == 0 && System.currentTimeMillis() - mDownTime > 50) {
                boolean isStartMove;
                if (isCollapsing) {
                    if (mDownY > DisplayUtils.dip2px(mContext, 220)) {
                        isStartMove = Math.abs(x - mDownX) < DisplayUtils.dip2px(mContext, 20) &&
                                y > mDownY;
                    } else {
                        isStartMove = Math.abs(x - mDownX) < DisplayUtils.dip2px(mContext, 6) &&
                                y - mDownY >= Math.abs(x - mDownX) * 2;
                    }
                } else {
                    isStartMove = System.currentTimeMillis() - mDownTime > 100 &&
                            mDownY - y >= Math.abs(x - mDownX);
                }

                if (isStartMove) {
                    mState = 1;
                    mMoveY = y;
                    mMoveTime = System.currentTimeMillis();
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(ev);
                    return true;
                }
            }

            if (mState == 0 && ev.getPointerCount() > 1) {
                mState = 2;
            }

            if (mState == 0 && System.currentTimeMillis() - mDownTime > 100) {
                boolean isEndMove;
                if (isCollapsing) {
                    isEndMove = Math.abs(x - mDownX) > DisplayUtils.dip2px(mContext, 3);
                } else {
                    isEndMove = mDownY - y < Math.abs(x - mDownX);
                }

                if (isEndMove) {
                    mState = 2;
                } else {
                    return true;
                }
            }

            if (mState == 1) {
                float offset = isCollapsing ? (y - mMoveY - flDetail.getHeight()) : (y - mMoveY);
                flDetail.setTranslationY(Math.max(Math.min(offset, 0), -flDetail.getHeight()));
                mImageViewer.setTranslationY(Math.max(Math.min(offset / 2, 0), -flDetail.getHeight()));
                titlePhotoViewer.setTranslationY(Math.max(Math.min(offset / 2, 0), -flDetail.getHeight()));
                return true;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP
                || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mState == 1) {
                float y = ev.getY();
                float progress = isCollapsing ? (y - mMoveY) : (mMoveY - y);
                if (progress >= flDetail.getHeight() / 3f ||
                        (progress >= DisplayUtils.dip2px(mContext, 10)
                                && System.currentTimeMillis() - mMoveTime < 300))
                    isCollapsing = !isCollapsing;
                setCollapsing(isCollapsing);
                return true;
            } else {
//                float x = ev.getX();
                float y = ev.getY();
//                Logger.d("_test_t_ " + Math.abs(x - mDownX) + ", " + (mDownY - y)
//                        + ", " + (System.currentTimeMillis() - mDownTime)
//                        + ", " + DisplayUtils.dip2px(mContext, 30)
//                        + ", " + DisplayUtils.dip2px(mContext, 120));
                if (//Math.abs(x - mDownX) < DisplayUtils.dip2px(mContext, 30) &&
//                        && mDownY - y > DisplayUtils.dip2px(mContext, 60)
                        System.currentTimeMillis() - mDownTime < 100) {
                    if (isCollapsing && mDownY - y < -DisplayUtils.dip2px(mContext, 120)
                            || !isCollapsing && mDownY - y > DisplayUtils.dip2px(mContext, 120)) {
                        setCollapsing(!isCollapsing);
                    }
                }
            }
            mState = 2;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setCollapsing(boolean isCollapsing) {
        this.isCollapsing = isCollapsing;
        mImageViewer.draggable(!isCollapsing);
        ObjectAnimator mDimViewAnim = ObjectAnimator.ofFloat(flDetail,
                "translationY", isCollapsing ? -flDetail.getHeight() : 0);
        mDimViewAnim.setDuration(300);
        mDimViewAnim.start();

        ObjectAnimator mImageViewerAnim = ObjectAnimator.ofFloat(mImageViewer,
                "translationY", isCollapsing ? -flDetail.getHeight() / 2f : 0);
        mImageViewerAnim.setDuration(300);
        mImageViewerAnim.start();

        ObjectAnimator mTitleAnim = ObjectAnimator.ofFloat(titlePhotoViewer,
                "translationY", isCollapsing ? -flDetail.getHeight() / 2f : 0);
        mTitleAnim.setDuration(300);
        mTitleAnim.start();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mState == 1) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onBackPressed() {
        if (isCollapsing) {
            setCollapsing(false);
            return true;
        }
        return mImageViewer.onBackPressed();
    }

    public interface OnItemChangedListener {
        View onItemChanged(int position);

        void onItemDeleted(int position);
    }

    public interface OnPhotoViewerListener {
        void onStateChanged(boolean open);
    }
}