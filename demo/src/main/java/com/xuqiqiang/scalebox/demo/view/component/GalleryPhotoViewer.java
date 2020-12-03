package com.xuqiqiang.scalebox.demo.view.component;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.snailstudio2010.imageviewer.IPhotoEntity;
import com.snailstudio2010.imageviewer.PhotoViewer;
import com.xuqiqiang.scalebox.demo.R;
import com.xuqiqiang.scalebox.demo.model.entity.PhotoEntity;
import com.xuqiqiang.uikit.utils.ArrayUtils;
import com.xuqiqiang.uikit.utils.BitmapUtils;
import com.xuqiqiang.uikit.utils.IntentUtils;
import com.xuqiqiang.uikit.utils.TimeUtils;
import com.xuqiqiang.uikit.view.dialog.BaseDialog;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.snailstudio2010.imageviewer.IPhotoEntity.TYPE_IMAGE;
import static com.xuqiqiang.uikit.utils.StringUtils.getFormatSize;

/**
 * Created by xuqiqiang on 2020/07/22.
 */
public class GalleryPhotoViewer extends PhotoViewer {

    private RecyclerView rvEdit;
    private TextView tvFileName;
    private TextView tvFileTime;
    private TextView tvFileSize;
    private TextView tvFileType;
    private TextView tvFileDevice;
    private View flFileSize;

    private List<PhotoEditItem> mOptionList;
    private PhotoEditAdapter mOptionAdapter;

    public GalleryPhotoViewer(Context context) {
        this(context, null, 0);
    }

    public GalleryPhotoViewer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GalleryPhotoViewer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initOption();
    }

    @Override
    protected int detailLayoutId() {
        return R.layout.photo_viewer_detail;
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        View menu = LayoutInflater.from(mContext).inflate(R.layout.photo_viewer_menu, llInfo);
        rvEdit = menu.findViewById(R.id.rv_edit);
        tvFileName = view.findViewById(R.id.tv_file_name);
        tvFileTime = view.findViewById(R.id.tv_file_time);
        tvFileSize = view.findViewById(R.id.tv_file_size);
        tvFileType = view.findViewById(R.id.tv_file_type);
        tvFileDevice = view.findViewById(R.id.tv_file_device);
        flFileSize = view.findViewById(R.id.fl_file_size);
    }

    @Override
    protected void refreshPhotoInfo(IPhotoEntity photoEntity) {
        initFileInfo(photoEntity);
        if (mOptionList != null) {
            if (mOptionList.get(3).isDisabled() == (photoEntity.getType() == TYPE_IMAGE)) {
                mOptionList.get(3).setDisabled(photoEntity.getType() != TYPE_IMAGE);
                mOptionAdapter.notifyItemChanged(3);
            }
        }
    }

    private void initFileInfo(IPhotoEntity photoEntity) {

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

    private void initOption() {
        GridLayoutManager manager = new GridLayoutManager(mContext, 5);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        rvEdit.setLayoutManager(manager);

        mOptionList = ArrayUtils.createList(
                new PhotoEditItem(R.mipmap.photo_share, "分享"),
                new PhotoEditItem(R.mipmap.photo_delete, "删除"),
                new PhotoEditItem(R.mipmap.photo_share, "收藏"),
                new PhotoEditItem(R.mipmap.photo_edit, "编辑"),
                new PhotoEditItem(R.mipmap.photo_more, "更多")
        );
        mOptionAdapter = new PhotoEditAdapter(mContext, mOptionList);
        // for test
        mOptionAdapter.setOnItemClickListener(new com.xuqiqiang.uikit.view.listener.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                final IPhotoEntity photoEntity = mPhotoList.get(mImageViewer.getCurrentPosition());
                if (position == 0) {
                    if (photoEntity.getType() == IPhotoEntity.TYPE_IMAGE) {
                        IntentUtils.sharePic(mContext, photoEntity.getFilePath());
                    } else if (photoEntity.getType() == PhotoEntity.TYPE_VIDEO) {
                        IntentUtils.shareVideo(mContext, photoEntity.getFilePath());
                    }
                } else if (position == 1) {
                    new BaseDialog.Builder(mContext)
                            .setTitle("删除")
                            .setMessage("是否删除此图片")
                            .setPositiveButton("删除", 0xFFD33636, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    if (FileUtils.delete(photoEntity.getFilePath())) {
//                                    } else {
//                                        ToastMaster.showToast(mContext, "无法删除");
//                                    }
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .create().show();
                } else if (position == 2) {
                } else if (position == 3) {
                } else if (position == 4) {
                }
            }
        });
        rvEdit.setAdapter(mOptionAdapter);
    }
}