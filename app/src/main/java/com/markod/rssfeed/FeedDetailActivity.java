package com.markod.rssfeed;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;

public class FeedDetailActivity extends AppCompatActivity {

    private String link;
    private String feedTitle;
    private String pubDate;
    private String description;
    private String channelTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_detail);
        if (savedInstanceState != null) {
            Log.d("AAAAAAA", "DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
            link = savedInstanceState.getString("link");
            feedTitle = savedInstanceState.getString("feedTitle");
            pubDate = savedInstanceState.getString("pubDate");
            description = savedInstanceState.getString("description");
            channelTitle = savedInstanceState.getString("channelTitle");
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Feed detail");
        Log.d("AAAAAAA", "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG " + savedInstanceState);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            feedTitle = intent.getStringExtra("title");
            pubDate = intent.getStringExtra("pubDate");
            description = intent.getStringExtra("description");
            link = intent.getStringExtra("link");
            channelTitle = intent.getExtras().getString("channelTitle", "No description available");
        }
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

    public void onLinkClick(View view) {

        Intent intent = new Intent (getBaseContext(), RssWebViewActivity.class);
        intent.putExtra("feedUrl", link);
        intent.putExtra("feedTitle", feedTitle);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d("AAAAAAA", "JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLJJJ");
        savedInstanceState.putString("link", link);
        savedInstanceState.putString("feedTitle", feedTitle);
        savedInstanceState.putString("pubDate", pubDate);
        savedInstanceState.putString("description", description);
        savedInstanceState.putString("channelTitle", channelTitle);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("AAAAAAA", "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("AAAAAAA", "PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
    }
}
