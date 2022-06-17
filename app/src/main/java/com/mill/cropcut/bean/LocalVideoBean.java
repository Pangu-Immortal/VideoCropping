package com.mill.cropcut.bean;

import android.graphics.Bitmap;

/**
 * Created by lulei-ms on 2017/8/23.
 */

public class LocalVideoBean {
    public Bitmap coverImage;
    public long duration; //单位：ms
    public String src_path;
    public long fileSize;
    public int fps;
    public int bitrate;
    public int width;
    public int height;

    @Override
    public String toString() {
        return "LocalVideoBean{" +
                " duration=" + duration +
                ", src_path='" + src_path + '\'' +
                ", fileSize=" + fileSize +
                ", fps=" + fps +
                ", bitrate=" + bitrate +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
