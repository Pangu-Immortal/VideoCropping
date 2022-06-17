package com.mill.cropcut.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mill.cropcut.R;
import com.mill.cropcut.adapter.VDurationCutAdapter;
import com.mill.cropcut.bean.LocalVideoBean;
import com.mill.cropcut.utils.CutUtils;
import com.mill.cropcut.utils.VideoCropHelper;
import com.mill.cropcut.utils.VideoFFCrop;

/**
 * 时长选择
 * Created by lulei-ms on 2017/8/23.
 */
public class VDurationCutView extends RelativeLayout implements RangeSlider.OnRangeChangeListener {
    public static final int THUMB_COUNT = 10;

    public interface IOnRangeChangeListener {
        void onKeyDown();

        void onKeyUp(int startTime, int endTime);
    }

    private Context mContext;

    private TextView mTvTip;
    private RecyclerView mRecyclerView;
    private RangeSlider mRangeSlider;

    private long mVideoDuration;
    private long mVideoStartPos;
    private long mVideoEndPos;

    private VDurationCutAdapter mAdapter;

    private IOnRangeChangeListener mRangeChangeListener;

    public VDurationCutView(Context context) {
        super(context);

        init(context);
    }

    public VDurationCutView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public VDurationCutView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    public void setRangeChangeListener(IOnRangeChangeListener listener) {
        mRangeChangeListener = listener;
    }

    private void init(Context context) {
        mContext = context;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_edit_view, this, true);

        mTvTip = (TextView) findViewById(R.id.tv_tip);

        mRangeSlider = (RangeSlider) findViewById(R.id.range_slider);
        mRangeSlider.setRangeChangeListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new VDurationCutAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);
    }

    public int getSegmentFrom() {
        return (int) mVideoStartPos;
    }

    public int getSegmentTo() {
        return (int) mVideoEndPos;
    }

    public void setMediaFileInfo(LocalVideoBean videoInfo) {
        if (videoInfo == null) {
            return;
        }
        mVideoDuration = videoInfo.duration;

        mVideoStartPos = 0;
        mVideoEndPos = mVideoDuration;

        VideoCropHelper.getLocalVideoBitmap(videoInfo.src_path, VDurationCutView.THUMB_COUNT, 120, 120, new VideoCropHelper.OnBitmapListener() {
            @Override
            public void onBitmapGet(Bitmap bitmap) {
                addBitmap(mAdapter.getItemCount(), bitmap);
            }
        });
    }



    public void addBitmap(int index, Bitmap bitmap) {
        mAdapter.add(index, bitmap);
    }

    @Override
    public void onKeyDown(int type) {
        if (mRangeChangeListener != null) {
            mRangeChangeListener.onKeyDown();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAdapter != null) {
            Log.d(VideoFFCrop.TAG, "onDetachedFromWindow: 清除所有bitmap");
            mAdapter.recycleAllBitmap();
        }
    }

    @Override
    public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
        int leftTime = (int) (mVideoDuration * leftPinIndex / 100); //ms
        int rightTime = (int) (mVideoDuration * rightPinIndex / 100);

        if (type == RangeSlider.TYPE_LEFT) {
            mVideoStartPos = leftTime;
        } else {
            mVideoEndPos = rightTime;
        }
        if (mRangeChangeListener != null) {
            mRangeChangeListener.onKeyUp((int) mVideoStartPos, (int) mVideoEndPos);
        }
        Log.d(VideoFFCrop.TAG, "onKeyUp: " + leftTime + "===" + rightTime);
        mTvTip.setText(String.format("左侧 : %s, 右侧 : %s ", CutUtils.duration(leftTime), CutUtils.duration(rightTime)));
    }

}
