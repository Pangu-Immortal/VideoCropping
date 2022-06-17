package com.mill.cropcut

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mill.cropcut.bean.LocalVideoBean
import com.mill.cropcut.utils.VideoCropHelper
import com.mill.cropcut.utils.VideoFFCrop
import com.mill.cropcut.utils.VideoFFCrop.FFListener
import com.mill.cropcut.view.VDurationCutView
import com.mill.cropcut.view.VDurationCutView.IOnRangeChangeListener
import com.mill.cropcut.view.VHwCropView


private const val TAG = "VideoCropPreActivity"

class VideoCropPreActivity : AppCompatActivity(), View.OnClickListener, IOnRangeChangeListener {



    private var srcVideo = Environment.getExternalStorageDirectory().absolutePath+ "testvideo.mp4"

    private var mVCropView: VHwCropView? = null
    private var mCutView: VDurationCutView? = null
    private var mCropBtn: TextView? = null
    private var mLocalVideoInfo: LocalVideoBean? = null



    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoFFCrop.instance?.init(this)
        setContentView(R.layout.video_crop)

        srcVideo = intent.extras?.getString("src").toString()


        mVCropView = findViewById<View>(R.id.crop_view) as VHwCropView
        mCutView = findViewById<View>(R.id.cut_view) as VDurationCutView
        mCropBtn = findViewById<View>(R.id.tv_ok) as TextView
        mCropBtn!!.setOnClickListener(this)
        mCutView!!.setRangeChangeListener(this)
        mLocalVideoInfo = VideoCropHelper.getLocalVideoInfo(srcVideo)
        this.mLocalVideoInfo?.duration?.let {
            mVCropView!!.videoView.setLocalPath(
                srcVideo,
                it.toInt()
            )
        }
        mCutView!!.setMediaFileInfo(mLocalVideoInfo)

    }














    override fun onResume() {
        super.onResume()
        mVCropView?.videoView?.start()
    }

    override fun onPause() {
        super.onPause()
        mVCropView?.videoView?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mVCropView?.videoView?.stopPlayback()
    }

    override fun onClick(v: View) {
        Log.d(
            TAG,
            "mVideoView " + mLocalVideoInfo?.width + "===" + mLocalVideoInfo?.height
        )
        VideoCropHelper.cropWpVideo(
            this@VideoCropPreActivity,
            mLocalVideoInfo!!,
            mVCropView!!,
            object : FFListener {
                override fun onProgress(progress: Int?) {
                Log.d(TAG, "progress: $progress")
                    mCropBtn!!.text = "progress: $progress"
                }


                override fun onFinish() {
                    Log.d(TAG, "finished")
                    mCropBtn!!.text = "finished"
                }

                override fun onFail(msg: String?) {
                    Log.d(TAG, "failed")
                    mCropBtn!!.text = "failed"
                }
            })
    }

    override fun onKeyDown() {}
    override fun onKeyUp(startTime: Int, endTime: Int) {
        mVCropView!!.setStarEndPo(startTime, endTime)
    }
}