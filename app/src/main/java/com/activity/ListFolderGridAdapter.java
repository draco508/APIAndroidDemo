package com.activity;

/**
 * Created by QuangPH on 2025-08-20
 */

import android.content.Context;
import android.content.Intent;
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

public class ListFolderGridAdapter extends RecyclerView.Adapter<ListFolderGridAdapter.GridViewHolder> {

    private List<VideoFolder> items;
    private Context context;

    public ListFolderGridAdapter(Context context, List<VideoFolder> items) {

        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public GridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.folder_item_grid, parent, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridViewHolder holder, int position) {
        VideoFolder item = items.get(position);
        holder.tvFolderPath.setText(item.getFolderName());
        holder.itemView.setOnClickListener( v -> {
            Intent intent = new Intent(context, TestActivity.class);
            intent.putExtra("folderPath", item.getFolderPath());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {
        TextView tvFolderPath;
        ImageView imageView;

        public GridViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolderPath = itemView.findViewById(R.id.tv_folder_path);
        }
    }
}

