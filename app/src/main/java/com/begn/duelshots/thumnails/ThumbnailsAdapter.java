package com.begn.duelshots.thumnails;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.begn.duelshots.R;

import java.util.List;

public class ThumbnailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static int lastPosition = -1;
    private ThumbnailCallback thumbnailCallback;
    private List<ThumbnailItem> dataSet;

    public ThumbnailsAdapter(List<ThumbnailItem> dataSet, ThumbnailCallback thumbnailCallback) {
        this.dataSet = dataSet;
        this.thumbnailCallback = thumbnailCallback;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.thumbnail_item,parent,false);
        return new ThumbnailsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ThumbnailItem thumbnailItem = dataSet.get(position);

        ThumbnailsViewHolder thumbnailsViewHolder = (ThumbnailsViewHolder) holder;
        thumbnailsViewHolder.thumbnail.setImageBitmap(thumbnailItem.image);
        thumbnailsViewHolder.thumbnail.setScaleType(ImageView.ScaleType.FIT_START);
//        if(lastPosition == position) {
//            thumbnailsViewHolder.itemView.setBackgroundColor(Color.parseColor("#3498db"));
//        }
        thumbnailsViewHolder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("POSITONS","Last Postion"+lastPosition + " and Present Position "+ position);
                if (lastPosition != position) {

                    thumbnailCallback.onThumbnailClick(thumbnailItem.filters);
                    lastPosition = position;
                }
            }

        });
    }


    @Override
    public int getItemCount() {
        return dataSet.size();
    }
    public static class ThumbnailsViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnail;

        public ThumbnailsViewHolder(View v) {
            super(v);
            this.thumbnail =  v.findViewById(R.id.thumbnail);
    }
    }
}
