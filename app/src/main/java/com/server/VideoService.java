package com.server;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Size;

import androidx.core.content.ContextCompat;

import com.activity.VideoFolder;
import com.activity.VideoItem;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideoService {

    private static VideoService instance;
    private final Context context;
    private final Gson gson = new Gson();

    private VideoService(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized VideoService getInstance(Context context) {
        if (instance == null) {
            instance = new VideoService(context);
        }
        return instance;
    }

    // MARK: - Public APIs

    /** Lấy tất cả video theo kiểu paging và trả về JSON string */
    public String getAllVideos(int page, Integer pageSize) throws Exception {
        ensureAuthorization();

        ContentResolver resolver = context.getContentResolver();

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";
        Cursor cursor = resolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
        );

        if (cursor == null) {
            throw new Exception("FetchFailed");
        }

        int startIndex = 0;
        int endIndex = cursor.getCount();
        if (pageSize != null) {
            startIndex = page * pageSize;
            endIndex = Math.min(startIndex + pageSize, cursor.getCount());
            if (startIndex >= endIndex) {
                cursor.close();
                return "[]";
            }
        }

        cursor.moveToPosition(startIndex);
        StringBuilder jsonArray = new StringBuilder("[");
        boolean first = true;

        for (int i = startIndex; i < endIndex; i++) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

            VideoItem item = makeVideoItem(id, duration);
            String json = gson.toJson(item);

            if (!first) jsonArray.append(",");
            jsonArray.append(json);
            first = false;

            cursor.moveToNext();
        }

        cursor.close();
        jsonArray.append("]");
        return jsonArray.toString();
    }

    /** Lấy video theo ID */
    public String getVideoById(String videoId) throws Exception {
        ensureAuthorization();

        ContentResolver resolver = context.getContentResolver();
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION
        };

        Cursor cursor = resolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Video.Media._ID + "=?",
                new String[]{videoId}, null
        );

        if (cursor == null || !cursor.moveToFirst()) {
            throw new Exception("FetchFailed");
        }

        long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));

        VideoItem item = makeVideoItem(id, duration);
        cursor.close();
        return gson.toJson(item);
    }

    // MARK: - Permission Helpers

    private void ensureAuthorization() throws Exception {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED) {
            throw new Exception("NotAuthorized");
        }
    }

    // MARK: - Private Helpers

    private VideoItem makeVideoItem(long id, long durationMs) {
        // Convert duration to mm:ss
        int durationSec = (int) (durationMs / 1000);
        int minutes = durationSec / 60;
        int seconds = durationSec % 60;
        String durationStr = String.format("%02d:%02d", minutes, seconds);

        String thumbnailBase64 = "";
        try {
            Bitmap thumbnail;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ dùng ContentResolver.loadThumbnail
                thumbnail = context.getContentResolver().loadThumbnail(
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                        new Size(160, 90),
                        null
                );
            } else {
                // Android 9 trở xuống dùng ThumbnailUtils
                String path = getRealPathFromId(id);
                thumbnail = android.media.ThumbnailUtils.createVideoThumbnail(
                        path,
                        MediaStore.Images.Thumbnails.MINI_KIND
                );
            }

            if (thumbnail != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] imageBytes = baos.toByteArray();
                thumbnailBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new VideoItem(String.valueOf(id), durationStr, thumbnailBase64);
    }

    /** Lấy real path từ video ID (chỉ Android 9 trở xuống mới cần) */
    private String getRealPathFromId(long id) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(
                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                projection, null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int colIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            String path = cursor.getString(colIndex);
            cursor.close();
            return path;
        }
        return null;
    }

    // MARK: - Save & Load Local JSON

    /** Save một VideoItem xuống local file */
    public void saveVideoToLocal(String json, String fileName) throws IOException {
        VideoItem video = gson.fromJson(json, VideoItem.class);
        String encodedJson = gson.toJson(video);

        File file = new File(context.getFilesDir(), fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(encodedJson.getBytes());
        fos.close();

        System.out.println("✅ Saved video " + video.id + " to: " + file.getAbsolutePath());
    }

    /** Load một VideoItem từ local file */
    public VideoItem loadVideoFromLocal(String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        if (!file.exists()) throw new IOException("File not found: " + fileName);

        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        String json = new String(bytes);
        return gson.fromJson(json, VideoItem.class);
    }

    // MARK: - VideoItem Model
    public List<VideoFolder> getVideoFolders(Context context) {
        List<VideoFolder> folders = new ArrayList<>();
        HashMap<String, Integer> folderMap = new HashMap<>();

        String[] projection = {
                MediaStore.Video.Media.DATA // đường dẫn file
        };

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );

        if (cursor != null) {
            int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

            while (cursor.moveToNext()) {
                String filePath = cursor.getString(columnIndexData);

                File file = new File(filePath);
                String folderPath = file.getParent();

                if (folderPath != null) {
                    // Đếm số video trong từng folder
                    if (folderMap.containsKey(folderPath)) {
                        folderMap.put(folderPath, folderMap.get(folderPath) + 1);
                    } else {
                        folderMap.put(folderPath, 1);
                    }
                }
            }
            cursor.close();
        }

        // Convert HashMap thành list VideoFolder
        for (String folderPath : folderMap.keySet()) {
            String folderName = new File(folderPath).getName();
            int count = folderMap.get(folderPath);
            folders.add(new VideoFolder(folderPath, folderName, count));
        }

        return folders;
    }

    /** Lấy toàn bộ video trong một folder */
    public List<VideoItem> getVideosInFolder(String folderPath) throws Exception {
        ensureAuthorization();

        List<VideoItem> videos = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATA
        };

        // Lọc các video có DATA (đường dẫn file) bắt đầu bằng folderPath
        Cursor cursor = resolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                MediaStore.Video.Media.DATA + " like ? ",
                new String[]{ folderPath + "%" },
                MediaStore.Video.Media.DATE_ADDED + " DESC"
        );

        if (cursor != null) {
            int idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idCol);
                long duration = cursor.getLong(durationCol);

                VideoItem item = makeVideoItem(id, duration);
                videos.add(item);
            }
            cursor.close();
        }

        return videos;
    }


}

