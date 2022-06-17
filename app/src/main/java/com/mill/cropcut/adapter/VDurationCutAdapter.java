package com.mill.cropcut.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.mill.cropcut.R;
import com.mill.cropcut.view.VDurationCutView;

import java.util.ArrayList;

/**
 * Created by lulei-ms on 2017/8/23.
 */

public class VDurationCutAdapter extends RecyclerView.Adapter<VDurationCutAdapter.ViewHolder> {
    private final Context mContext;
    private ArrayList<Bitmap> data = new ArrayList<Bitmap>();

    public VDurationCutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final int itemCount = VDurationCutView.THUMB_COUNT;
        int padding = mContext.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        final int itemWidth = (screenWidth - 2 * padding) / itemCount;
        int height = mContext.getResources().getDimensionPixelOffset(R.dimen.ugc_item_thumb_height);
//        int height = (int) (itemWidth / VideoCropHelper.WHA);
        ImageView view = new ImageView(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(itemWidth, height));
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.thumb.setImageBitmap(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void add(int position, Bitmap b) {
        data.add(b);
        notifyItemInserted(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumb;

        public ViewHolder(View itemView) {
            super(itemView);
            thumb = (ImageView) itemView;
        }
    }

    public void addAll(ArrayList<Bitmap> bitmap) {
        recycleAllBitmap();

        data.addAll(bitmap);
        notifyDataSetChanged();
    }

    public void recycleAllBitmap() {
        for (Bitmap b : data) {
            if (!b.isRecycled())
                b.recycle();
        }
        data.clear();
        notifyDataSetChanged();
    }
}
