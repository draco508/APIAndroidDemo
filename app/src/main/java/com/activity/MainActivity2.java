package com.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.application.ApplicationController;
import com.diff.demoapplication.R;


public class MainActivity2 extends AppCompatActivity {


    ValueCallback<Uri[]> vc;

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.browser);
        String url = String.format("http://localhost:%s/demo.html", ApplicationController.getInstance().hostPort);
        Log.d("Url +++ :", url);
        initWebView(webView,url);
    }

    @SuppressLint("JavascriptInterface")
    public void initWebView(WebView webView, String url) {
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(wcc);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

    WebChromeClient wcc = new WebChromeClient() {

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            Intent fileChooserIntent = fileChooserParams.createIntent();
            startActivityForResult(fileChooserIntent, 112);
            vc = filePathCallback;
            return true;

        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri[] selectedFileUri = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
        vc.onReceiveValue(selectedFileUri);
    }

}