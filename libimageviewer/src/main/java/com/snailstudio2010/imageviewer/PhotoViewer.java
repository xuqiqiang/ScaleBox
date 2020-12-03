package com.snailstudio2010.imageviewer;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.xuqiqiang.uikit.utils.BitmapUtils;
import com.xuqiqiang.uikit.utils.DisplayUtils;
import com.xuqiqiang.uikit.utils.Logger;
import com.xuqiqiang.uikit.utils.MimeUtils;
import com.xuqiqiang.uikit.utils.ScreenUtils;
import com.xuqiqiang.uikit.utils.StringUtils;
import com.xuqiqiang.uikit.utils.ViewUtils;

import java.util.Arrays;
import java.util.List;

import indi.liyi.viewer.ImageDrawee;
import indi.liyi.viewer.ImageTransfer;
import indi.liyi.viewer.ImageViewer;
import indi.liyi.viewer.R;
import indi.liyi.viewer.ViewData;
import indi.liyi.viewer.ViewerStatus;
import indi.liyi.viewer.listener.OnBrowseStatusListener;
import indi.liyi.viewer.listener.OnItemClickListener;

/**
 * Created by xuqiqiang on 2020/07/22.
 */
@SuppressWarnings("unused")
public class PhotoViewer extends FrameLayout {
    private static final int STATE_IDLE = 0, STATE_SCROLL = 1, STATE_CANCEL = 2;

    private final ImageViewerHelper mImageViewerHelper;
    private final GlideImageLoader mImageLoader;
    protected Activity mContext;
    protected List<? extends IPhotoEntity> mPhotoList;
    protected OnItemChangedListener mOnItemChangedListener;
    protected ImageViewer mImageViewer;
    protected TextView tvTitle;
    protected ViewGroup llInfo;
    private View flDetail;
    private NestedScrollView svDetail;
    private View titlePhotoViewer;
    private ImageInfo mImageInfo;
    private boolean isImageShowing;
    private OnPhotoViewerListener mOnPhotoViewerListener;
    private View mItemButton;
    private float mDownX;
    private float mDownY;
    private float mMoveY;
    private long mDownTime;
    private long mMoveTime;
    private int mState = STATE_IDLE;
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
        View view = inflater.inflate(layoutId(), this);
        int detailLayoutId = detailLayoutId();
        if (detailLayoutId != 0) {
            ViewGroup flDetailContent = view.findViewById(R.id.fl_detail_content);
            inflater.inflate(detailLayoutId, flDetailContent);
        }
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
                IPhotoEntity photoEntity = mPhotoList.get(position);
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

        mImageViewerHelper = new ImageViewerHelper(mContext);
        mImageLoader = new GlideImageLoader();
        mImageLoader.setOnItemButtonListener(new GlideImageLoader.OnItemButtonListener() {
            @Override
            public void onItemButtonChange(View button) {
                mItemButton = button;
            }

            @Override
            public void onPlay(View button) {
                onPlayVideo(mImageViewer.getCurrentPosition());
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

    @LayoutRes
    protected int layoutId() {
        return R.layout.photo_viewer;
    }

    @LayoutRes
    protected int detailLayoutId() {
        return 0;
    }

    @CallSuper
    protected void initView(View view) {
        mImageViewer = view.findViewById(R.id.image_viewer);
        llInfo = view.findViewById(R.id.ll_info);
        tvTitle = view.findViewById(R.id.tv_title);
        flDetail = view.findViewById(R.id.fl_detail);
        svDetail = view.findViewById(R.id.sv_detail);
        titlePhotoViewer = view.findViewById(R.id.title_photo_viewer);
    }

    protected void onPlayVideo(int position) {
    }

    protected void refreshPhotoInfo(IPhotoEntity photoEntity) {
        tvTitle.setText(StringUtils.getShortName(photoEntity.getFilePath()));
    }

    public void setOnItemChangedListener(OnItemChangedListener onItemChangedListener) {
        this.mOnItemChangedListener = onItemChangedListener;
    }

    public void setOnPhotoViewerListener(OnPhotoViewerListener onPhotoViewerListener) {
        this.mOnPhotoViewerListener = onPhotoViewerListener;
    }

    public void show(final List<? extends IPhotoEntity> photoList, final int index, final View view) {
        if (index < 0 || index >= photoList.size()) return;
        mPhotoList = photoList;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                IPhotoEntity[] arr = new IPhotoEntity[photoList.size()];
                for (int i = 0; i < arr.length; i += 1) {
                    arr[i] = photoList.get(i);
                }
                mImageInfo = mImageViewerHelper.initImageInfo(arr, index, view);
                mImageInfo.setShowIndex(false);
                int[] size = null;
                if (view != null) {
                    size = (int[]) view.getTag();
                } else {
                    for (ImageInfo.Info info : mImageInfo.getList()) {
                        Point s = BitmapUtils.getBitmapSize(((IPhotoEntity) info.getSrc()).getFilePath());
                        info.setWidth(DisplayUtils.dip2px(mContext, 100));
                        info.setHeight(info.getWidth() * s.y / (float) s.x);
                        info.setX((ScreenUtils.getWidth() - info.getWidth()) / 2);
                        info.setY(ScreenUtils.getHeight());
                    }
                }
                Logger.d("_test_i_ size 1:" + Arrays.toString(size));
                if (size == null || size[0] == size[1]) {
                    String filePath = photoList.get(index).getFilePath();
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

    public List<? extends IPhotoEntity> getPhotoList() {
        return mPhotoList;
    }

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
                mState = STATE_CANCEL;
            } else {
                mState = STATE_IDLE;
                mDownTime = System.currentTimeMillis();
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float x = ev.getX();
            float y = ev.getY();
            if (mState == STATE_IDLE && System.currentTimeMillis() - mDownTime > 50) {
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
                    mState = STATE_SCROLL;
                    mMoveY = y;
                    mMoveTime = System.currentTimeMillis();
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(ev);
                    return true;
                }
            }

            if (mState == STATE_IDLE && ev.getPointerCount() > 1) {
                mState = STATE_CANCEL;
            }

            if (mState == STATE_IDLE && System.currentTimeMillis() - mDownTime > 100) {
                boolean isEndMove;
                if (isCollapsing) {
                    isEndMove = Math.abs(x - mDownX) > DisplayUtils.dip2px(mContext, 3);
                } else {
                    isEndMove = mDownY - y < Math.abs(x - mDownX);
                }

                if (isEndMove) {
                    mState = STATE_CANCEL;
                } else {
                    return true;
                }
            }

            if (mState == STATE_SCROLL) {
                float offset = isCollapsing ? (y - mMoveY - flDetail.getHeight()) : (y - mMoveY);
                flDetail.setTranslationY(Math.max(Math.min(offset, 0), -flDetail.getHeight()));
                mImageViewer.setTranslationY(Math.max(Math.min(offset / 2, 0), -flDetail.getHeight()));
                titlePhotoViewer.setTranslationY(Math.max(Math.min(offset / 2, 0), -flDetail.getHeight()));
                return true;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP
                || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            float y = ev.getY();
            if (mState == STATE_SCROLL) {
                float progress = isCollapsing ? (y - mMoveY) : (mMoveY - y);
                if (progress >= flDetail.getHeight() / 3f ||
                        (progress >= DisplayUtils.dip2px(mContext, 10)
                                && System.currentTimeMillis() - mMoveTime < 300))
                    isCollapsing = !isCollapsing;
                setCollapsing(isCollapsing);
                return true;
            } else {
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
            mState = STATE_CANCEL;
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
        if (mState == STATE_SCROLL) {
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