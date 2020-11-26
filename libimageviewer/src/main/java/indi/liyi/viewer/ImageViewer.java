package indi.liyi.viewer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.utils.BitmapUtils;
import com.xuqiqiang.uikit.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import indi.liyi.viewer.dragger.DragHandler;
import indi.liyi.viewer.dragger.DragMode;
import indi.liyi.viewer.dragger.DragStatus;
import indi.liyi.viewer.listener.OnBrowseStatusListener;
import indi.liyi.viewer.listener.OnDragStatusListener;
import indi.liyi.viewer.listener.OnItemChangedListener;
import indi.liyi.viewer.listener.OnItemClickListener;
import indi.liyi.viewer.listener.OnItemLongPressListener;
import indi.liyi.viewer.otherui.DefaultIndexUI;
import indi.liyi.viewer.otherui.DefaultProgressUI;
import indi.liyi.viewer.otherui.IndexUI;
import indi.liyi.viewer.otherui.ProgressUI;
import indi.liyi.viewer.scimgv.PhotoView;
import indi.liyi.viewer.viewpager.ImagePagerAdapter;
import indi.liyi.viewer.viewpager.ImageViewPager;
import indi.liyi.viewer.viewpager.NoPreloadViewPager;

public class ImageViewer extends FrameLayout implements NoPreloadViewPager.OnPageChangeListener {
    public static Handler mMainHandler;
    private final String TAG = getClass().getSimpleName();
    // 索引视图
    private IndexUI indexUI;
    // 加载进度视图
    private ProgressUI progressUI;
    private ImageViewPager viewPager;
    private ImagePagerAdapter mAdapter;
    private ImageLoader mLoader;
    private DragHandler mDragHandler;
    // 是否执行进场动画
    private boolean playEnterAnim = true;
    // 是否执行退场动画
    private boolean playExitAnim = true;
    // 动画执行时间
    private long mDuration = 300;
    // 是否显示图片索引
    private boolean showIndex = true;
    // 是否可拖拽图片
    private boolean draggable = true;
    // 拖拽模式
    private int mDragMode = DragMode.MODE_AGILE;
    private List<ViewData> mSourceList;
    // ImageViewer 是否会占据 StatusBar 的空间
    private boolean overlayStatusBar = false;
    private float mMaxScale;
    private float mMinScale;
    // 上一次的触摸点坐标
    private float mLastX, mLastY;
    // 是否正在进行拖拽
    private boolean isDragging;
    // 是否有动画正在执行
    private boolean hasAnimRunning;
    // 是否已经执行了进场动画
    private boolean hasPlayEnterAnim = false;
    // ImageViewer 的当前状态
    private int mViewStatus = ViewerStatus.STATUS_SILENCE;
    // 缓存 item，用于复用
    private ArrayList<ImageDrawee> mViewBox = new ArrayList<ImageDrawee>();
    private OnItemClickListener mItemClickListener;
    private OnItemLongPressListener mItemLongPressListener;
    private OnItemChangedListener mItemChangedListener;
    private OnDragStatusListener mDragStatusListener;
    private OnBrowseStatusListener mBrowseStatusListener;
    private ImageTransfer.OnTransCallback mOnTransListener;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private List<Runnable> mRunnableOnShow = new ArrayList<>();
    private float mOnTransProgress;
    private boolean isInAnim;
    private int mCurConfigureItem = -1;
    private HandlerThread mBackgroundThread;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    private int mStartPosition = -1;

    public ImageViewer(Context context) {
        super(context);
        init(context, null);
    }

    public ImageViewer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ImageViewer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    public void startBackgroundThread() {
        if (mBackgroundThread == null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    public void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ImageViewer);
            if (a != null) {
                playEnterAnim = a.getBoolean(R.styleable.ImageViewer_ivr_playEnterAnim, true);
                playExitAnim = a.getBoolean(R.styleable.ImageViewer_ivr_playExitAnim, true);
                mDuration = a.getInteger(R.styleable.ImageViewer_ivr_duration, 300);
                showIndex = a.getBoolean(R.styleable.ImageViewer_ivr_showIndex, true);
                draggable = a.getBoolean(R.styleable.ImageViewer_ivr_draggable, true);
                mDragMode = a.getInteger(R.styleable.ImageViewer_ivr_dragMode, DragMode.MODE_AGILE);
                a.recycle();
            }
        }
        initView();
        startBackgroundThread();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    private void initView() {
        if (getBackground() == null) {
            setBackgroundColor(Color.BLACK);
        }

        // 添加 ViewPager
        viewPager = new ImageViewPager(getContext());
//        viewPager.setOffscreenPageLimit(1);
        viewPager.setOnPageChangeListener(this);
        addView(viewPager, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        setVisibility(View.INVISIBLE);

        progressUI = new DefaultProgressUI();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrolled(i, v, i1);
    }

//    private Map<Integer, Object> map =new HashMap<>();

    @Override
    public void onPageSelected(final int i) {
        if (showIndex && indexUI != null && mSourceList != null) {
            indexUI.handleItemChanged(i, mSourceList.size());
        }
        if (mItemChangedListener != null) {
            mItemChangedListener.onItemChanged(i, getCurrentItem());
        }
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageSelected(i);

//        if (mCurIndex < 0) mCurIndex = i;
//        ImageDrawee drawee = (ImageDrawee) (map.get(i));
//        if(drawee != null)
//        configureItem(i, drawee, 2);//mViewBox.get(i % 3));
//        map.remove(i - 2);
//        map.remove(i + 2);

//        configureItem(i, getCurrentItem(), 2);
//        map.clear();
//        if(i>0)
//        map.put(i -1, mAdapter.instantiateItem(viewPager, i -1));
//        map.put(i +1, mAdapter.instantiateItem(viewPager, i +1));


//        if(viewPager.getOffscreenPageLimit() == 0)
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                viewPager.setOffscreenPageLimit(1);
//            }
//        }, 2000);
////        viewPager.setOffscreenPageLimit(1);
//        mCurIndex = i;
        Log.d(TAG, "_testiv_ onPageSelected:" + i);
        mBackgroundHandler.removeCallbacksAndMessages(null);
        mBackgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                mMainHandler.removeCallbacksAndMessages(null);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ImageDrawee drawee = getCurrentItem();
                        if (drawee != null)
                            configureItem(viewPager.getCurrentItem(), drawee, 2);
                        else {
                            // 时机过早
                            mMainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "_testiv_ onPageSelected delayed:" + i);
                                    configureItem(viewPager.getCurrentItem(), getCurrentItem(), 2);
                                }
                            }, 300);
                        }
                    }
                });
//                        configureItem(mCurIndex, getCurrentItem(), 2);
            }
        });
    }

    @Override
    public void onPageScrollStateChanged(int i) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrollStateChanged(i);
//        Log.d(TAG, "onPageScrollStateChanged:" + i + ", getCurrentItem:" + viewPager.getCurrentItem() + ",mCurIndex:" + mCurIndex);
        if (i != ViewPager.SCROLL_STATE_IDLE) {
            isInAnim = true;
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isInAnim = false;
                }
            }, 500);
//            int curItem = viewPager.getCurrentItem();
//            if (mCurIndex != curItem) {
//                mCurIndex = curItem;
//                mBackgroundHandler.removeCallbacksAndMessages(null);
//                mBackgroundHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mMainHandler.removeCallbacksAndMessages(null);
//                        mMainHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                configureItem(mCurIndex, getCurrentItem(), 2);
//                            }
//                        });
////                        configureItem(mCurIndex, getCurrentItem(), 2);
//                    }
//                });
//            }
        }
//        isInAnim = i != ViewPager.SCROLL_STATE_IDLE;
    }

    public ImageViewer imageData(@NonNull List list) {
        if (mSourceList == null) {
            mSourceList = new ArrayList<ViewData>();
        } else {
            mSourceList.clear();
        }
        for (int i = 0, len = list.size(); i < len; i++) {
            ViewData vd = new ViewData(list.get(i));
            mSourceList.add(vd);
        }
        return this;
    }

    public ImageViewer overlayStatusBar(boolean overlayStatusBar) {
        this.overlayStatusBar = overlayStatusBar;
        return this;
    }

    public ImageViewer bindViewGroup(@NonNull ViewGroup viewGroup) {
        for (int i = 0, len = viewGroup.getChildCount(); i < len; i++) {
            View child = viewGroup.getChildAt(i);
            int[] location = new int[2];
            // 获取 child 在屏幕中的坐标，其中 getLocationOnScreen() 方法获取的 y 轴坐标包含状态栏的高度
            child.getLocationOnScreen(location);
            mSourceList.get(i).setTargetX(location[0]);
            mSourceList.get(i).setTargetY(overlayStatusBar ? location[1] : location[1] - Utils.getStatusBarHeight(getContext()));
            mSourceList.get(i).setTargetWidth(child.getMeasuredWidth());
            mSourceList.get(i).setTargetHeight(child.getMeasuredHeight());
        }
        return this;
    }

    public ImageViewer viewData(@NonNull List<ViewData> list) {
        this.mSourceList = list;
        return this;
    }

    public ImageViewer imageLoader(@NonNull ImageLoader loader) {
        this.mLoader = loader;
        return this;
    }

    public ImageViewer playEnterAnim(boolean play) {
        this.playEnterAnim = play;
        return this;
    }

    public ImageViewer playExitAnim(boolean play) {
        this.playExitAnim = play;
        return this;
    }

    public ImageViewer duration(long duration) {
        this.mDuration = duration;
        return this;
    }

    public ImageViewer showIndex(boolean showIndex) {
        this.showIndex = showIndex;
        return this;
    }

    public ImageViewer loadIndexUI(@NonNull IndexUI indexUI) {
        this.indexUI = indexUI;
        return this;
    }

    public ImageViewer loadProgressUI(@NonNull ProgressUI progressUI) {
        this.progressUI = progressUI;
        return this;
    }

    public ImageViewer draggable(boolean draggable) {
        this.draggable = draggable;
        return this;
    }

    public ImageViewer dragMode(int mode) {
        this.mDragMode = mode;
        return this;
    }

    public ImageViewer setMaxScale(float scale) {
        this.mMaxScale = scale;
        return this;
    }

    public ImageViewer setMinScale(float scale) {
        this.mMinScale = scale;
        return this;
    }

    public ImageViewer setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
        return this;
    }

    public ImageViewer setOnItemLongPressListener(OnItemLongPressListener listener) {
        this.mItemLongPressListener = listener;
        return this;
    }

    public ImageViewer setOnItemChangedListener(OnItemChangedListener listener) {
        this.mItemChangedListener = listener;
        return this;
    }

    public ImageViewer setOnDragStatusListener(OnDragStatusListener listener) {
        this.mDragStatusListener = listener;
        return this;
    }

    public ImageViewer setOnBrowseStatusListener(OnBrowseStatusListener listener) {
        this.mBrowseStatusListener = listener;
        return this;
    }

    public ImageViewer setOnTransListener(ImageTransfer.OnTransCallback listener) {
        this.mOnTransListener = listener;
        return this;
    }

    public ImageViewer setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        this.mOnPageChangeListener = listener;
        return this;
    }

    public void postOnShow(Runnable r) {
        if (viewPager.getOffscreenPageLimit() == 0)
            mRunnableOnShow.add(r);
        else
            r.run();
    }

    /**
     * 开启浏览
     *
     * @param startPosition 开始位置，即点击的 ImageView 的位置
     */
    public void watch(@IntRange(from = 0) int startPosition) {
        watch(startPosition, 0, 0, null);
    }

    /**
     * 开启浏览
     *
     * @param startPosition 开始位置，即点击的 ImageView 的位置
     * @param callback      进场动画执行回调
     */
    public void watch(@IntRange(from = 0) int startPosition, ImageTransfer.OnTransCallback callback) {
        watch(startPosition, 0, 0, callback);
    }

    /**
     * 开启浏览
     *
     * @param startPosition    开始位置，即点击的 ImageView 的位置
     * @param startImageWidth  被点击的 ImageView 加载的资源图片的实际宽度
     * @param startImageHeight 被点击的 ImageView 加载的资源图片的实际高度
     */
    public void watch(@IntRange(from = 0) int startPosition, int startImageWidth, int startImageHeight) {
        watch(startPosition, startImageWidth, startImageHeight, null);
    }

    /**
     * 开启浏览
     *
     * @param startPosition    开始位置，即点击的 ImageView 的位置
     * @param startImageWidth  被点击的 ImageView 加载的资源图片的实际宽度
     * @param startImageHeight 被点击的 ImageView 加载的资源图片的实际高度
     * @param callback         进场动画执行回调
     */
    public void watch(@IntRange(from = 0) final int startPosition, int startImageWidth, int startImageHeight, final ImageTransfer.OnTransCallback callback) {
        if (mSourceList == null || startPosition >= mSourceList.size()) {
            Log.e(TAG, "SourceList is null or StartPosition greater than or equal to the length of Sourcelist.");
            return;
        }
        Log.d(TAG, "_testiv_ watch:" + startPosition);
        mCurConfigureItem = -1;
        if (startImageWidth != 0 && startImageHeight != 0
                && (mSourceList.get(startPosition).getImageWidth() == 0 ||
                mSourceList.get(startPosition).getImageHeight() == 0)) {
            mSourceList.get(startPosition).setImageWidth(startImageWidth);
            mSourceList.get(startPosition).setImageHeight(startImageHeight);
        }
        viewPager.setScrollable(true);
        mStartPosition = startPosition;
        mAdapter = new ImagePagerAdapter(mSourceList.size()) {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
//                if(map.get(position)!= null) return map.get(position);
                return ImageViewer.this.instantiateItem(container, position, callback);
            }
        };
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(startPosition, false);
//        mCurIndex = startPosition;
        setVisibility(VISIBLE);
        if (!playEnterAnim) {
            noteBrowseStatus(ViewerStatus.STATUS_WATCHING);
            handleIndexUI(startPosition);
        }
    }

    /**
     * 取消浏览
     */
    public void cancel() {
        cancel(null);
    }

    /**
     * 取消浏览
     *
     * @param callback 退场动画执行回调
     */
    public void cancel(final ImageTransfer.OnTransCallback callback) {
        if (viewPager.getOffscreenPageLimit() > 0)
            viewPager.setOffscreenPageLimit(0);
        if (indexUI != null) {
            indexUI.hide();
        }
        if (playExitAnim) {
            if (mSourceList == null) return;
            int position = getCurrentPosition();
            new ImageTransfer(getWidth(), getHeight())
                    .with(getCurrentItem().getImageView())
                    .background(getBackground())
                    .duration(mDuration)
                    .loadExitData(mSourceList.get(position))
                    .callback(new ImageTransfer.OnTransCallback() {
                        @Override
                        public void onStart() {
                            noteBrowseStatus(ViewerStatus.STATUS_BEGIN_CLOSE);
                            if (callback != null) {
                                callback.onStart();
                            }
                            if (mOnTransListener != null) {
                                mOnTransListener.onStart();
                            }
                            isInAnim = true;
                        }

                        @Override
                        public void onRunning(float progress) {
                            noteBrowseStatus(ViewerStatus.STATUS_CLOSING);
                            if (callback != null) {
                                callback.onRunning(progress);
                            }
                            if (mOnTransListener != null) {
                                mOnTransListener.onRunning(1 - progress);
                                mOnTransProgress = 1 - progress;
                            }
                        }

                        @Override
                        public void onEnd() {
                            noteBrowseStatus(ViewerStatus.STATUS_SILENCE);
                            if (callback != null) {
                                callback.onEnd();
                            }
                            if (mOnTransListener != null) {
                                mOnTransListener.onEnd();
                            }
                            shutComplete();
                            isInAnim = false;
                        }
                    })
                    .play();
        } else {
            noteBrowseStatus(ViewerStatus.STATUS_SILENCE);
            shutComplete();
        }
    }

    /**
     * 实例化 item
     */
    private ImageDrawee instantiateItem(ViewGroup container, int position, final ImageTransfer.OnTransCallback callback) {
        ImageDrawee drawee = null;
        Log.d(TAG, "_testiv_ instantiateItem:" + position);
        if (mViewBox.size() > 0) {
            for (ImageDrawee item : mViewBox) {
                if (item.getParent() == null) {
                    drawee = item;
                    break;
                }
            }
        }
        if (drawee == null) {
            drawee = new ImageDrawee(container.getContext());
            drawee.setProgressUI(progressUI);
            mViewBox.add(drawee);
        }
//        map.put(position, drawee);
        container.addView(drawee);
        configureItem(position, drawee, mStartPosition == position ? 0 : 1);
        if (playEnterAnim && !hasPlayEnterAnim && mStartPosition == position) {
            hasPlayEnterAnim = true;
            new ImageTransfer(getWidth(), getHeight())
                    .with(drawee.getImageView())
                    .loadEnterData(mSourceList.get(position))
                    .background(getBackground())
                    .duration(mDuration)
                    .callback(new ImageTransfer.OnTransCallback() {
                        @Override
                        public void onStart() {
                            noteBrowseStatus(ViewerStatus.STATUS_BEGIN_OPEN);
                            if (callback != null) {
                                callback.onStart();
                            }
                            if (mOnTransListener != null) {
                                mOnTransListener.onStart();
                            }
                            isInAnim = true;
                        }

                        @Override
                        public void onRunning(float progress) {
                            noteBrowseStatus(ViewerStatus.STATUS_OPENING);
                            if (callback != null) {
                                callback.onRunning(progress);
                            }
                            if (mOnTransListener != null) {
                                mOnTransListener.onRunning(progress);
                                mOnTransProgress = progress;
                            }
                        }

                        @Override
                        public void onEnd() {
                            handleIndexUI(mStartPosition);
                            noteBrowseStatus(ViewerStatus.STATUS_WATCHING);
                            if (callback != null) {
                                callback.onEnd();
                            }
                            if (mOnTransListener != null) {
                                mOnTransListener.onEnd();
                            }
                            if (viewPager.getOffscreenPageLimit() == 0)
                                viewPager.setOffscreenPageLimit(1);

                            if (mOnPageChangeListener != null && mStartPosition == 0)
                                mOnPageChangeListener.onPageSelected(0);
                            mStartPosition = -1;

                            if (!ArrayUtils.isEmpty(mRunnableOnShow)) {
                                for (Runnable r : mRunnableOnShow) r.run();
                                mRunnableOnShow.clear();
                            }
                            isInAnim = false;
                        }
                    })
                    .play();
        } else {
            if (mStartPosition == position) {
                mStartPosition = -1;
            }
        }
        return drawee;
    }

    // Fix: java.lang.RuntimeException: Canvas: trying to use a recycled bitmap android.graphics.Bitmap@dcdd2bc
    public void onResume() {
        int position = getCurrentPosition();
        if (position > 0)
            configureItem(position - 1, getItem(position - 1), 1);
        if (position < mSourceList.size() - 1)
            configureItem(position + 1, getItem(position + 1), 1);
    }

    /**
     * 配置 item
     */
    private void configureItem(final int position, final ImageDrawee drawee, final int state) {
        if (drawee == null) return;
        Log.d(TAG, "configureItem " + position + "," + state);
        if (state == 0 || state == 2) {
            if (position == mCurConfigureItem) return;
            mCurConfigureItem = position;
        }
        drawee.setTag(position);
        if (mMaxScale > 0) {
            drawee.setMaxScale(mMaxScale);
        }
        if (mMinScale > 0) {
            drawee.setMinScale(mMinScale);
        }
        Log.d(TAG, "configureItem exec " + position + "," + state);
        mLoader.displayImage(mSourceList.get(position).getImageSrc(),
                state,//viewPager.getCurrentItem() == position ? 0 : 1,
                drawee.getImageView(), new ImageLoader.LoadCallback() {
                    @Override
                    public void onLoadStarted(Object placeholder) {
                        drawee.setImage(placeholder);
                    }

                    @Override
                    public void onLoading(float progress) {
                        drawee.handleProgress(progress);
                    }

                    @Override
                    public boolean onLoadSucceed(Object source) {
                        if (mSourceList == null) return true;
                        if ((state == 0 || state == 2)) {
//                    if (source instanceof Bitmap)
//                        ((Bitmap) source).recycle();
                            Log.d(TAG, "onLoadSucceed position:" + position + ",getCurrentItem:" + viewPager.getCurrentItem());
                            if (position != viewPager.getCurrentItem()) {
                                return true;
                            }

                            PhotoView photoView = (PhotoView) drawee.getImageView();
                            if (photoView.isTouchScaleEnabled()) {
                                int width = 0;
                                if (source instanceof Drawable) {
                                    width = ((Drawable) source).getIntrinsicWidth();
                                } else if (source instanceof Bitmap) {
                                    width = ((Bitmap) source).getWidth();
                                }
                                if (width > 0) {
                                    drawee.setMaxScale(Math.max(width / (float) ScreenUtils.getWidth() * 3f, 3f));
                                    Log.d(TAG, "onLoadSucceed setMaxScale:" + (width / (float) ScreenUtils.getWidth() * 3f));
                                }
                            }

                            if (source instanceof Bitmap) {
                                final Bitmap resource = (Bitmap) source;
                                int maxSize = BitmapUtils.getMaxBitmapSize();
                                if (!resource.isRecycled() && (resource.getWidth() > maxSize * 0.8
                                        || resource.getHeight() > maxSize * 0.8)) {
                                    if (position != viewPager.getCurrentItem()) {
                                        return true;
                                    }
                                    mBackgroundHandler.removeCallbacksAndMessages(null);
                                    mBackgroundHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Bitmap bitmap = null;
                                            if (!resource.isRecycled()) {
                                                try {
                                                    bitmap = resource.copy(Bitmap.Config.ARGB_8888, false);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            if (bitmap == null || position != viewPager.getCurrentItem()) {
                                                return;
                                            }
                                            final Bitmap finalBitmap = bitmap;
                                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (position != viewPager.getCurrentItem()) {
                                                        return;
                                                    }
                                                    setImage(finalBitmap);
                                                }
                                            });
                                        }
                                    });
                                    return true;
                                }
                            }
                        }

//                PhotoView photoView = (PhotoView) drawee.getImageView();
//                if(photoView.isTouchScaleEnabled()) {
//                    drawee.setMaxScale(Math.max(bitmap.getWidth() / (float) ScreenUtils.getWidth() * 3f, 3f));
//                    Log.d(TAG, "loadBitmap setMaxScale:" + (bitmap.getWidth() / (float) ScreenUtils.getWidth()));
//                }
//                ViewParent viewParent = imageView.getParent();
//                if (viewParent instanceof ImageDrawee) {
//                    ((ImageDrawee) viewParent).setMaxScale(Math.max(bitmap.getWidth() / (float) ScreenUtils.getWidth() * 3f, 3f));
//                    Log.d(TAG, "loadBitmap setMaxScale:" + (bitmap.getWidth() / (float) ScreenUtils.getWidth()));
//                }

                        return setImage(source);
//                boolean isGif = Utils.isGif(mSourceList.get(position).getImageSrc());
//                drawee.hideProgressUI();
//                if (!isGif) drawee.setImage(source);
//                if (mSourceList.get(position).getImageWidth() == 0 ||
//                        mSourceList.get(position).getImageHeight() == 0) {
//                    Drawable drawable = drawee.getImageView().getDrawable();
//                    if (drawable != null) {
//                        mSourceList.get(position).setImageWidth(drawable.getIntrinsicWidth());
//                        mSourceList.get(position).setImageHeight(drawable.getIntrinsicHeight());
//                    }
//                }
//                return !isGif;
                    }

                    private boolean setImage(Object source) {
                        boolean isGif = Utils.isGif(mSourceList.get(position).getImageSrc());
                        drawee.hideProgressUI();
                        if (!isGif) drawee.setImage(source);
                        if (mSourceList.get(position).getImageWidth() == 0 ||
                                mSourceList.get(position).getImageHeight() == 0) {
                            Drawable drawable = drawee.getImageView().getDrawable();
                            if (drawable != null) {
                                mSourceList.get(position).setImageWidth(drawable.getIntrinsicWidth());
                                mSourceList.get(position).setImageHeight(drawable.getIntrinsicHeight());
                            }
                        }
                        return !isGif;
                    }

                    @Override
                    public void onLoadFailed(Object error) {
                        drawee.hideProgressUI();
                        drawee.setImage(error);
                    }
                });
        // 单击事件
        drawee.getImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasAnimRunning && mItemClickListener != null) {
                    final boolean result = mItemClickListener.onItemClick(position, drawee.getImageView());
                    // 判断是否消费了单击事件，如果消费了，则单击事件的后续方法不执行
                    if (result) {
                        return;
                    }
                }
                cancel();
            }
        });
        // 长按事件
        drawee.getImageView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!hasAnimRunning && mItemLongPressListener != null) {
                    return mItemLongPressListener.onItemLongPress(position, drawee.getImageView());
                }
                return false;
            }
        });
    }

    private void noteDragStatus(int status) {
        switch (status) {
            case DragStatus.STATUS_BEGIN_RESTORE:
            case DragStatus.STATUS_BEGIN_EXIT:
                hasAnimRunning = true;
                break;
            case DragStatus.STATUS_COMPLETE_RESTORE:
            case DragStatus.STATUS_COMPLETE_EXIT:
                hasAnimRunning = false;
                break;
        }
        if (mDragStatusListener != null) {
            mDragStatusListener.onDragStatusChanged(status);
        }
    }

    private void noteBrowseStatus(int status) {
        mViewStatus = status;
        switch (status) {
            case ViewerStatus.STATUS_BEGIN_OPEN:
                hasAnimRunning = true;
                break;
            case ViewerStatus.STATUS_BEGIN_CLOSE:
                viewPager.setScrollable(false);
                hasAnimRunning = true;
                break;
            case ViewerStatus.STATUS_WATCHING:
                hasAnimRunning = false;
                break;
            case ViewerStatus.STATUS_SILENCE:
                viewPager.setScrollable(true);
                hasAnimRunning = false;
                break;
        }
        if (mBrowseStatusListener != null) {
            mBrowseStatusListener.onBrowseStatus(mViewStatus);
        }
    }

    /**
     * 处理索引 UI
     */
    private void handleIndexUI(int startPosition) {
        if (indexUI == null) {
            indexUI = new DefaultIndexUI(overlayStatusBar);
        }
        if (showIndex) {
            if (mSourceList.size() > 1) {
                indexUI.setup(this, startPosition, mSourceList.size());
            } else {
                indexUI.hide();
            }
        } else {
            indexUI.hide();
        }
    }

    private void shutComplete() {
        setVisibility(GONE);
        reset();
    }

    private void reset() {
        if (mViewBox.size() > 0) {
            mViewBox.clear();
        }
        if (mDragHandler != null) {
            mDragHandler.clear();
            mDragHandler = null;
        }
        mAdapter = null;
        hasAnimRunning = false;
        hasPlayEnterAnim = false;
    }

    public List<ViewData> getViewData() {
        return mSourceList;
    }

    public ImageDrawee getCurrentItem() {
        return getItem(getCurrentPosition());
    }

    public ImageDrawee getItem(int position) {
        if (mViewBox != null) {
            for (ImageDrawee drawee : mViewBox) {
                if (drawee.getTag() != null && (int) drawee.getTag() == position) {
                    return drawee;
                }
            }
        }
        return (ImageDrawee) viewPager.findViewWithTag(position);
    }

    public int getCurrentPosition() {
        return viewPager != null ? viewPager.getCurrentItem() : 0;
    }

    public int getViewStatus() {
        return mViewStatus;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////  拖拽相关
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = super.onInterceptTouchEvent(ev);
        if (isInAnim) {
            mLastX = 0;
            mLastY = 0;
            return isIntercept;
        }
        if (!hasAnimRunning && draggable) {
            // 是否拦截触摸事件？
            // 若拦截，则 ImageViewer 自己处理触摸事件；
            // 若不拦截，则 ImageView 处理触摸事件
            int action = ev.getAction() & ev.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                mLastX = ev.getX();
                mLastY = ev.getY();
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mLastX == 0 && mLastY == 0) return isIntercept;
                /**
                 * 拖拽触发条件：
                 * 1.允许拖拽
                 * 2.仅有一个触摸点
                 * 3.图片的缩放等级 = 1f
                 * 4.拖拽处理类不为空
                 * 5.手势为上下滑动手势，左右滑动不触发
                 */
                if (ev.getPointerCount() == 1 && getCurrentItem().getScale() <= 1f) {
                    float disX = ev.getX() - mLastX;
                    float disY = ev.getY() - mLastY;
                    // 上下滑动手势
                    if (Math.abs(disX) < Math.abs(disY)) {
                        if (mDragMode == DragMode.MODE_AGILE && disY < 0) {
                            return isIntercept;
                        }
                        isDragging = true;
                        if (mDragHandler == null) {
                            mDragHandler = new DragHandler(getWidth(), getHeight());
                            mDragHandler.setDragListener(new DragHandler.DragListener() {
                                @Override
                                public void onDrag(float alpha) {
                                    if (mOnTransListener != null)
                                        mOnTransListener.onRunning(alpha);
                                    mOnTransProgress = alpha;
                                }
                            });
                        }
                        mDragHandler.onReay(mDragMode, getBackground());
                        noteDragStatus(DragStatus.STATUS_READY);
                        isIntercept = true;
                    }
                }
            }
        }
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isInAnim) {
            mLastX = 0;
            mLastY = 0;
            return super.onTouchEvent(event);
        }
        if (mLastX == 0 && mLastY == 0) return super.onTouchEvent(event);
        int action = event.getAction() & event.getActionMasked();
        if (action == MotionEvent.ACTION_MOVE) {
            if (draggable && isDragging && mDragHandler != null) {
                mDragHandler.onDrag(mLastX, mLastY, event, getCurrentItem().getImageView());
                noteDragStatus(DragStatus.STATUS_DRAGGING);
            }
            mLastX = event.getX();
            mLastY = event.getY();
        } else if (action == MotionEvent.ACTION_UP) {
            if (draggable && isDragging && mDragHandler != null) {
                isDragging = false;
                // 释放图片
                mDragHandler.onUp(getCurrentItem().getImageView(),
                        mSourceList.get(getCurrentPosition()), new TransCallback(false),
                        new TransCallback(true));
            }
            mLastX = 0;
            mLastY = 0;
        }
        return super.onTouchEvent(event);
    }

    private void handleDragResultStatus(String proceed) {
        if (mDragHandler != null) {
            if (mDragHandler.getAction() == ImageTransfer.ACTION_DRAG_RESTORE) {
                int status = proceed.equals("start") ? DragStatus.STATUS_BEGIN_RESTORE :
                        (proceed.equals("running") ? DragStatus.STATUS_RESTORING :
                                DragStatus.STATUS_COMPLETE_RESTORE);
                if (status == DragStatus.STATUS_BEGIN_RESTORE) {
                    viewPager.setScrollable(false);
                    hasAnimRunning = true;
                } else if (status == DragStatus.STATUS_COMPLETE_RESTORE) {
                    viewPager.setScrollable(true);
                    hasAnimRunning = false;
                }
                noteDragStatus(status);
            } else if (mDragHandler.getAction() == ImageTransfer.ACTION_DRAG_EXIT_AGILE
                    || mDragHandler.getAction() == ImageTransfer.ACTION_DRAG_EXIT_SIMPLE) {
                int status = proceed.equals("start") ? DragStatus.STATUS_BEGIN_EXIT :
                        (proceed.equals("running") ? DragStatus.STATUS_EXITTING :
                                DragStatus.STATUS_COMPLETE_EXIT);
                if (status == DragStatus.STATUS_BEGIN_EXIT) {
                    viewPager.setScrollable(false);
                    hasAnimRunning = true;
                } else if (status == DragStatus.STATUS_COMPLETE_EXIT) {
                    viewPager.setScrollable(true);
                    hasAnimRunning = false;
                }
                noteDragStatus(status);
                if (proceed.equals("end")) {
                    noteBrowseStatus(ViewerStatus.STATUS_SILENCE);
                    if (viewPager.getOffscreenPageLimit() > 0)
                        viewPager.setOffscreenPageLimit(0);
                    shutComplete();
                }
            }
        }
    }

    /**
     * 如果本方法未执行，则是因为图片浏览器为获取到焦点，可在外部手动获取焦点
     * 建议在外部手动调动本方法
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (onBackPressed()) return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onBackPressed() {
        if (!hasAnimRunning) {
            if (mViewStatus == ViewerStatus.STATUS_WATCHING) {
                cancel();
                // 消费返回键点击事件，不传递出去
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mSourceList = null;
        mLoader = null;
        reset();
        stopBackgroundThread();
    }

    private class TransCallback implements ImageTransfer.OnTransCallback {
        private boolean isExit;

        public TransCallback(boolean isExit) {
            this.isExit = isExit;
        }

        @Override
        public void onStart() {
            handleDragResultStatus("start");
            isInAnim = true;
        }

        @Override
        public void onRunning(float progress) {
            handleDragResultStatus("running");
            if (isExit) {
                mOnTransProgress = mOnTransProgress * (1 - progress);
            } else {
                mOnTransProgress = mOnTransProgress * (1 - progress) + progress;
            }
            if (mOnTransListener != null)
                mOnTransListener.onRunning(mOnTransProgress);
        }

        @Override
        public void onEnd() {
            handleDragResultStatus("end");
            isInAnim = false;
        }
    }
}
