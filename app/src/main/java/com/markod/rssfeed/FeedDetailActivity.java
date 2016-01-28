package com.markod.rssfeed;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;

public class FeedDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Feed detail");
        Intent intent = getIntent();
        String feedTitle = intent.getStringExtra("title");
        String pubDate = intent.getStringExtra("pubDate");
        String description = intent.getStringExtra("description");
        String link = intent.getStringExtra("link");
        String channelTitle = intent.getExtras().getString("channelTitle", "No description available");
        TextView titleView = (TextView) findViewById(R.id.textTitle);
        TextView pubDateView = (TextView) findViewById(R.id.textPubDate);
        TextView descriptionView = (TextView) findViewById(R.id.textDescription);
        TextView linkView = (TextView) findViewById(R.id.textLink);
        ImageView sourceImageView = (ImageView) findViewById(R.id.sourceImageView);
        File channelImageFile = new File(getFilesDir().getAbsolutePath() + "/" + channelTitle + ".png");
        try {
            sourceImageView.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(channelImageFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        titleView.setText(feedTitle);
        pubDateView.setText(pubDate);
        descriptionView.setText(description);
        linkView.setText(link);
    }
}
