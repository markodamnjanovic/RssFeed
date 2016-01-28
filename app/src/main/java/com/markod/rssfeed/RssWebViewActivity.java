package com.markod.rssfeed;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import com.markod.rssfeed.fragments.WebViewFragment;

public class RssWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rss_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        String feedTitle = getIntent().getStringExtra("feedTitle");
        actionBar.setTitle(feedTitle);
        String feedUrl = getIntent().getStringExtra("feedUrl");
        Fragment webViewFragment = new WebViewFragment();
        Bundle data = new Bundle();
        data.putString("feedUrl", feedUrl);
        webViewFragment.setArguments(data);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.frameLayout, webViewFragment);
        ft.commit();
    }

}
