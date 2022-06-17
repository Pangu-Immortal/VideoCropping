package com.mill.cropcut.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.mill.cropcut.R;
import com.mill.cropcut.utils.VideoCropHelper;

/**
 * 视频播放器 & 尺寸裁切高亮框
 * Created by lulei-ms on 2017/8/23.
 */
public class VHwCropView extends FrameLayout implements LocalVideoView.OnVideoSizeChangeedListener, View.OnTouchListener {
    private LocalVideoView mVideoView;
    private OverlayView mOverlayView;

    private float lastX, lastY;

    public VHwCropView(Context context) {
        this(context, null);
    }

    public VHwCropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VHwCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.video_view_crop, this, true);

        mOverlayView = (OverlayView) findViewById(R.id.overlay_view);
        mVideoView = (LocalVideoView) findViewById(R.id.video_view);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView);
        mOverlayView.processStyledAttributes(a);
        a.recycle();

        mVideoView.setOnVideoSizeChangeedListener(this);

        mOverlayView.setShowCropGrid(false);
        mOverlayView.setTargetAspectRatio(VideoCropHelper.WHA);
        mOverlayView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getPointerCount() != 1) {
            return true;
        }
        //高亮部分 可以左右移动
        if (event.getAction()== MotionEvent.ACTION_MOVE) {
            float dx = event.getX() - lastX;
            float newLeft = mOverlayView.getCropViewRect().left + dx;
            float newRight = mOverlayView.getCropViewRect().right + dx;
            if(newLeft >= mOverlayView.getLeft() && newRight <= mOverlayView.getRight()) {
                mOverlayView.getCropViewRect().offset(dx, 0);
                mOverlayView.invalidate();
            }
        }
        lastX = event.getX();
        lastY = event.getY();
        return true;
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (mOverlayView != null && mOverlayView.getLayoutParams() != null) {
            //重新设置高亮部分，宽高
            int ovW = (int) (height * VideoCropHelper.WHA);
            int ovH = height;
            mOverlayView.setCropViewRect((width - ovW) / 2, (getHeight() - ovH) / 2, (width + ovW) / 2, (getHeight() + ovH) / 2);
        }
    }

    public void setStarEndPo(int start, int end) {
        mVideoView.setStarEndPo(start, end);
    }

    @Override
    protected void onDetachedFromWindow() {
        mVideoView.stopPlayback();
        super.onDetachedFromWindow();
    }

    public LocalVideoView getVideoView() {
        return mVideoView;
    }

    public OverlayView getOverlayView() {
        return mOverlayView;
    }


}
