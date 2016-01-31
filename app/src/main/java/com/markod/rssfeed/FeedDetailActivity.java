package com.markod.rssfeed;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

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
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            feedTitle = intent.getStringExtra("title");
            pubDate = intent.getStringExtra("pubDate");
            description = intent.getStringExtra("description");
            link = intent.getStringExtra("link");
            channelTitle = intent.getExtras().getString("channelTitle", "No description available");
            new  GetFavoriteValueForFeedAsyncTask().execute();
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

        Intent intent = new Intent(getBaseContext(), RssWebViewActivity.class);
        intent.putExtra("feedUrl", link);
        intent.putExtra("feedTitle", feedTitle);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("link", link);
        savedInstanceState.putString("feedTitle", feedTitle);
        savedInstanceState.putString("pubDate", pubDate);
        savedInstanceState.putString("description", description);
        savedInstanceState.putString("channelTitle", channelTitle);
    }

    public void onFavoriteClicked(View view) {
        TextView favoritedTextView = (TextView) findViewById(R.id.textFavorited);
        ImageButton favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
        if (favoritedTextView.getVisibility() == View.INVISIBLE) {
            favoritedTextView.setVisibility(View.VISIBLE);
            favoriteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorFavorite));
            new SaveUpdateFavoriteFeed().execute(1);
        } else {
            favoritedTextView.setVisibility(View.INVISIBLE);
            favoriteButton.setBackgroundColor(ContextCompat.getColor(this, R.color.colorButton));
            new SaveUpdateFavoriteFeed().execute(0);
        }

    }

    private class GetFavoriteValueForFeedAsyncTask extends AsyncTask<Void, Void, Boolean> {
        SQLiteDatabase db;
        Cursor cursor;

        @Override
        protected Boolean doInBackground(Void... params) {
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(FeedDetailActivity.this);
            try {
                 db = helper.getReadableDatabase();
                cursor = db.query("rss_feed_favorite", new String[]{"_id"}, "link = ? AND favorite = 1", new String[]{link}, null, null, null);
                return cursor.moveToFirst();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            cursor.close();
            db.close();
            TextView favoritedTextView = (TextView) findViewById(R.id.textFavorited);
            ImageButton favoriteButton = (ImageButton) findViewById(R.id.favoriteButton);
            if (success == null)
                Toast.makeText(FeedDetailActivity.this, "Database unavailable", Toast.LENGTH_LONG).show();
            else if (success) {
                favoritedTextView.setVisibility(View.VISIBLE);
                favoriteButton.setBackgroundColor(ContextCompat.getColor(FeedDetailActivity.this, R.color.colorFavorite));
            }
        }
    }

    private class SaveUpdateFavoriteFeed extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... favorites) {
            int favorite = favorites[0];
            ContentValues values = new ContentValues();
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(FeedDetailActivity.this);
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor cursor =  db.query("rss_feed_favorite", new String[]{"_id"}, "link = ?", new String[]{link}, null, null, null);
                if (cursor.moveToFirst()) {
                    values.put("favorite", favorite);
                    db.update("rss_feed_favorite", values, "link = ?", new String[]{link});
                } else {
                    values.put("link", link);
                    values.put("feed_title", feedTitle);
                    values.put("pub_date", pubDate);
                    values.put("description", description);
                    values.put("channel_title", channelTitle);
                    values.put("favorite", 1);
                    db.insert("rss_feed_favorite", null, values);

                }
                return true;

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) Toast.makeText(FeedDetailActivity.this, "Database unavailable", Toast.LENGTH_LONG).show();
        }
    }
}
