package com.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.diff.demoapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.server.VideoService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    private RecyclerView recyclerView;
    private VideoService videoService;

    private List<VideoFolder> folders;

    private static final int REQUEST_PERMISSION = 100;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout thay vì setContentView
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoService = VideoService.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.rvListFolder);

        // Grid 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // spacing 8dp
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_8);
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, spacingInPixels, true));

        getAllFolder();
    }

    private void getAllFolder() {
        new Thread(() -> {
            try {
                folders = videoService.getVideoFolders(requireContext());

                requireActivity().runOnUiThread(() -> {
                    Log.i("RunOnUIThread", "RunOnUIThread");
                    recyclerView.setAdapter(new ListFolderGridAdapter(requireContext(), this.folders));
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


}