package com.example.courseworklive.activities;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.courseworklive.R;

public class UserGuide extends AppCompatActivity {

    /**
     * This is the User Guide activity
     * The main reason for its existence is to show the user guide
     * Through the use of a WebView
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);
        WebView guide = findViewById(R.id.user_guide_view);
        guide.setInitialScale(1);
        guide.setWebViewClient(new WebViewClient());
        guide.setWebChromeClient(new WebChromeClient());
        guide.getSettings().setLoadsImagesAutomatically(true);
        guide.getSettings().setJavaScriptEnabled(true);
        guide.getSettings().setLoadWithOverviewMode(true);
        guide.getSettings().setUseWideViewPort(true);
        guide.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.133 Mobile Safari/535.19");
        guide.clearCache(true);
        getApplicationContext().deleteDatabase("webview.db");
        getApplicationContext().deleteDatabase("webviewCache.db");
        guide.loadUrl("file:///android_asset/user_guide.html");
    }

    /**
     * Destroying the page when exciting the guide*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}