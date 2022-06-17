package com.mill.cropcut

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mill.cropcut.utils.VideoFFCrop
import android.widget.EditText
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.View
import android.widget.Button
import com.mill.cropcut.utils.VideoFFCrop.FFListener
import android.widget.TextView
import java.lang.Exception

class VcActivity : AppCompatActivity(), View.OnClickListener {
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        VideoFFCrop.instance?.init(this)
        (findViewById<View>(R.id.runbtn) as Button).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val srcVideo = (findViewById<View>(R.id.editText1) as EditText).text.toString()
        val destPath = (findViewById<View>(R.id.editText3) as EditText).text.toString()
        var duration = 0
        var srcW = 0
        var srcH = 0
        val mmr = MediaMetadataRetriever()
        try {
            mmr.setDataSource(srcVideo)
            duration =
                (java.lang.Long.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000).toInt()
            srcW =
                Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
            srcH =
                Integer.valueOf(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
            Log.d(VideoFFCrop.TAG, "MediaMetadataRetriever $srcVideo===$duration===$srcW====$srcH")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mmr.release()
        }
        VideoFFCrop.instance?.cropVideo(this@VcActivity, srcVideo, destPath, 0, duration, object : FFListener {
                override fun onProgress(progress: Int?) {
                    Log.d("VcActivity", "progress: $progress");
                    (findViewById<View>(R.id.textView6) as TextView).text = "progress: $progress"
                }

                override fun onFinish() {
                    Log.d("VcActivity", "finished")
                    (findViewById<View>(R.id.textView6) as TextView).text = "finished"
                }

                override fun onFail(msg: String?) {
                    Log.d("VcActivity", "failed")
                    (findViewById<View>(R.id.textView6) as TextView).text = "failed"
                }
            })
    }
}