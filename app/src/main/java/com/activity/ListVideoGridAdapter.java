package com.activity;

/**
 * Created by QuangPH on 2025-08-20
 */

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.diff.demoapplication.R;
import java.util.List;

public class ListVideoGridAdapter extends RecyclerView.Adapter<ListVideoGridAdapter.GridViewHolder> {

    private List<VideoItem> items;

    public ListVideoGridAdapter(List<VideoItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_item_grid, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        VideoItem video = items.get(position);

        byte[] decodedBytes = android.util.Base64.decode(video.thumbnailBase64, android.util.Base64.DEFAULT);

        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(decodedBytes) // nạp vào Glide từ mảng byte
                .placeholder(R.drawable.ic_place_holder)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageView imageView;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            imageView = itemView.findViewById(R.id.ivThumb);

        }
    }
}

