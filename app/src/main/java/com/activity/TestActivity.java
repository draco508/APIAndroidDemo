package com.activity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.diff.demoapplication.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.server.VideoService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private VideoService videoService;

    private WebView webView;

    private List<VideoItem> videos;
    private static final int REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        videoService = VideoService.getInstance(this);
        videos = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);

        // Grid 2 cột
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        // spacing 8dp
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_8);
        recyclerView.addItemDecoration(new SpacingItemDecoration(2, spacingInPixels, true));


        checkPermissionAndLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return  true;
    }

    // Xử lý khi chọn menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_get_all_video) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_get_folder) {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);
        } else {
            //loadWebView();
            loadVideo();
        }
    }

    private  void loadVideo() {
        new Thread( () -> {
           try {
               String json = videoService.getAllVideos(0, 10);
               Log.d("TestActivity", "JSON = " + json);
               Type listType = new TypeToken<List<VideoItem>>() {}.getType();

               this.videos = new Gson().fromJson(json, listType);
               runOnUiThread(() -> {
                   Log.i("RunOnUIThread", "RunOnUIThread");
                   recyclerView.setAdapter(new ListVideoGridAdapter(this.videos));
               });

           } catch (Exception e) {
               e.printStackTrace();

           }
        }).start();
    }

    private void loadWebView() {
        String html = "<html>" +
                "<body>" +
                "<h2>VideoService Test</h2>" +
                "<button onclick=\"getAll()\">Get All Videos</button><br/><br/>" +
                "<button onclick=\"getById()\">Get Video by ID=1</button><br/><br/>" +
                "<button onclick=\"saveLocal()\">Save First Video</button><br/><br/>" +
                "<button onclick=\"loadLocal()\">Load Video</button><br/><br/>" +
                "<pre id='output'></pre>" +

                "<script>" +
                "function show(data){ document.getElementById('output').innerText = data; }" +
                "function getAll(){ AndroidAPI.getAllVideos(0,5); }" +
                "function getById(){ AndroidAPI.getVideoById('1'); }" +
                "function saveLocal(){ AndroidAPI.saveVideoToLocal('video.json'); }" +
                "function loadLocal(){ AndroidAPI.loadVideoFromLocal('video.json'); }" +
                "</script>" +
                "</body></html>";

       // webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    // Nhận kết quả xin quyền
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadWebView();
            } else {
                Toast.makeText(this, "Không có quyền đọc video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Bridge để JS gọi vào Android
    private class JSBridge {

        @JavascriptInterface
        public void getAllVideos(int page, int pageSize) {
            runOnUiThread(() -> {
                try {
                    String json = videoService.getAllVideos(page, pageSize);
                    webView.evaluateJavascript("show(" + toJsString(json) + ")", null);
                } catch (Exception e) {
                    webView.evaluateJavascript("show('Error: " + e.getMessage() + "')", null);
                }
            });
        }

        @JavascriptInterface
        public void getVideoById(String id) {
            runOnUiThread(() -> {
                try {
                    String json = videoService.getVideoById(id);
                    webView.evaluateJavascript("show(" + toJsString(json) + ")", null);
                } catch (Exception e) {
                    webView.evaluateJavascript("show('Error: " + e.getMessage() + "')", null);
                }
            });
        }

        @JavascriptInterface
        public void saveVideoToLocal(String fileName) {
            runOnUiThread(() -> {
                try {
                    String json = videoService.getAllVideos(0, 1); // lấy 1 video đầu tiên
                    videoService.saveVideoToLocal(json.substring(1, json.length()-1), fileName);
                    webView.evaluateJavascript("show('Saved to local: " + fileName + "')", null);
                } catch (Exception e) {
                    webView.evaluateJavascript("show('Error: " + e.getMessage() + "')", null);
                }
            });
        }

        @JavascriptInterface
        public void loadVideoFromLocal(String fileName) {
            runOnUiThread(() -> {
                try {
                    VideoItem item = videoService.loadVideoFromLocal(fileName);
                    String json = new com.google.gson.Gson().toJson(item);
                    webView.evaluateJavascript("show(" + toJsString(json) + ")", null);
                } catch (Exception e) {
                    webView.evaluateJavascript("show('Error: " + e.getMessage() + "')", null);
                }
            });
        }
    }

    // Escape JSON để chèn vào JS
    private String toJsString(String json) {
        return "\"" + json.replace("\"", "\\\"") + "\"";
    }
}
