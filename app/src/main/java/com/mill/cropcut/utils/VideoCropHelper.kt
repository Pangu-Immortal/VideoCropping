package com.mill.cropcut.utils

import android.content.Context
import com.mill.cropcut.bean.LocalVideoBean
import com.mill.cropcut.view.VHwCropView
import com.mill.cropcut.utils.VideoFFCrop.FFListener
import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import java.lang.Exception

/**
 * Created by lulei-ms on 2017/8/23.
 */
private const val TAG = "VideoCropHelper"

object VideoCropHelper {
    const val WHA = 2 / 3f //尺寸裁切成宽高比 2：3

    /**
     * 裁切横屏 视频
     *
     * @param context
     * @param videoBean
     * @param mVCropView
     * @param listener
     */
    fun cropWpVideo(
        context: Context,
        videoBean: LocalVideoBean,
        mVCropView: VHwCropView,
        listener: FFListener
    ) {
        if (videoBean == null) {
            Log.e(TAG, "cropWpVideo: videoBean=null");
            return
        }
        val srcVideo = videoBean.src_path.trim()
        var startPo = 0
        var duration = 0
        val srcW = videoBean.width
        val srcH = videoBean.height
        var width = 0
        var height = 0
        var x = 0
        var y = 0
//        if (srcW <= srcH) {
//            Log.e(TAG, "cropWpVideo: srcW <= srcH")
//            return
//        }
        width = (srcH * WHA).toInt()
        height = srcH
        if (mVCropView != null) {
            val rectF = mVCropView.overlayView.cropViewRect
            x = (srcW * rectF.left / mVCropView.width).toInt()
            startPo = mVCropView.videoView.startPo / 1000
            duration = mVCropView.videoView.endPo / 1000 - startPo
        } else {
            x = (srcW - width) / 2
            startPo = 0
            duration = (videoBean.duration / 1000).toInt()
        }
        duration = if (duration <= 0) 1 else duration //最小为1
        y = 0
        Log.d(VideoFFCrop.TAG, "Media $videoBean====$x")
        var start = srcVideo.lastIndexOf(".")
        if (start == -1) {
            start = srcVideo.length
        }
//        val destPath = srcVideo.substring(0, start) + "_wp.mp4"
        val destPath = Environment.getExternalStorageDirectory().path +"/1234/cc.mp4"
        FileUtils.createDir(Environment.getExternalStorageDirectory().path +"/1234")
        Log.d(TAG, "cropWpVideo: $destPath")
        VideoFFCrop.instance?.cropVideo(
            context,
            srcVideo,
            destPath,
            startPo,
            duration,
            listener
        )
    }

    /**
     * 获取本地视频信息
     */
    fun getLocalVideoInfo(path: String?): LocalVideoBean {
        val info = LocalVideoBean()
        info.src_path = path
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(path)
            info.duration =
                java.lang.Long.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
            info.width =
                Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            info.height =
                Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mmr.release()
        }
        return info
    }

    /**
     * 获取视频帧列表
     *
     * @param path
     * @param count    期望个数
     * @param width    期望压缩后宽度
     * @param height   期望压缩后高度
     * @param listener
     */
    @JvmStatic
    fun getLocalVideoBitmap(
        path: String?,
        count: Int,
        width: Int,
        height: Int,
        listener: OnBitmapListener?
    ) {
        val task: AsyncTask<Any?, Any?, Any?> = object : AsyncTask<Any?, Any?, Any?>() {

            override fun doInBackground(vararg params: Any?): Any? {
                val mmr = MediaMetadataRetriever()
                try {
                    mmr.setDataSource(path)
                    val duration =
                        java.lang.Long.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) * 1000
                    val inv = duration / count
                    var i: Long = 0
                    while (i < duration) {

                        //注意getFrameAtTime方法的timeUs 是微妙， 1us * 1000 * 1000 = 1s
                        val bitmap =
                            mmr.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        //                        Log.d(VideoFFCrop.TAG, "getFrameAtTime "+ i + "===" + bitmap.getWidth() + "===" + bitmap.getHeight());
                        val destBitmap = Bitmap.createScaledBitmap(bitmap!!, width, height, true)
                        Log.d(
                            VideoFFCrop.TAG,
                            "getFrameAtTime " + i + "===" + destBitmap.width + "===" + destBitmap.height
                        )
                        bitmap.recycle()
                        publishProgress(destBitmap)
                        i += inv
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                } finally {
                    mmr.release()
                }
                return null
            }

            override fun onProgressUpdate(vararg values: Any?) {
                listener?.onBitmapGet(values[0] as Bitmap)
            }

            override fun onPostExecute(result: Any?) {}
        }
        task.execute()
    }

    interface OnBitmapListener {
        fun onBitmapGet(bitmap: Bitmap?)
    }
}