package com.markod.rssfeed.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.markod.rssfeed.FeedDetailActivity;
import com.markod.rssfeed.R;
import com.markod.rssfeed.RssFeedDatabaseHelper;
import com.markod.rssfeed.rssHandler.RssItem;
import com.markod.rssfeed.rssHandler.RssReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class Tab1Fragment extends ListFragment {

    private ListView feedsListView;
    private SwipeRefreshLayout view;
    private List<RssItem> rssItems = new ArrayList<>();


    public Tab1Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         view = (SwipeRefreshLayout) inflater.inflate(R.layout.fragment_tab1, container, false);
        view.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RssHandlerAsyncTask().execute(getActivity());
             }
         });
        feedsListView = (ListView) view.findViewById(android.R.id.list);
            new RssHandlerAsyncTask().execute(getActivity());
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        feedsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RssItem item = rssItems.get(position);
                Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                intent.putExtra("title", item.getTitle());
                intent.putExtra("pubDate", item.getPubDate());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("link", item.getLink());
                intent.putExtra("channelTitle", item.getChannelTitle());
                startActivity(intent);
            }
        });
    }

    private class RssHandlerAsyncTask extends AsyncTask<Activity, Integer, List<RssItem>> {

        @Override
        protected List<RssItem> doInBackground(Activity... activities) {
            Activity activity = activities[0];
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(activity);
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor cursor = db.query("rss_feed", new String[]{"_id", "source_url"}, "show_feed = ?", new String[] {Integer.toString(1)}, null, null, null);
                rssItems.clear();
                while (cursor.moveToNext()) {
                    String sourceUrl = cursor.getString(1);
                    RssReader rssReader = new RssReader(sourceUrl);
                    rssItems.addAll(rssReader.getItems());
                }
                cursor.close();
                db.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return rssItems;
        }

        @Override
        protected void onPostExecute(List<RssItem> rssItems) {
            RssFeedsArrayAdapter adapter = new RssFeedsArrayAdapter(getActivity(), R.layout.rss_feed_list_item, rssItems);
            feedsListView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            view.setRefreshing(false);
        }
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;
        private String imageFileName;

        public ImageLoadTask(String url, ImageView imageView, String imageFileName) {
            this.url = url;
            this.imageView = imageView;
            this.imageFileName = imageFileName;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
            //save channel image to internal storage
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(getActivity().getFilesDir().getAbsolutePath() + "/" + imageFileName + ".png");
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }

    }

    private class GetFavoriteValueForFeedAsyncTask extends AsyncTask<Object, Void, Boolean> {

        View listItemView;
        Cursor cursor;
        SQLiteDatabase db;

        @Override
        protected Boolean doInBackground(Object... objects) {
            listItemView = (View) objects[0];
            String link = (String) objects[1];
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(getActivity());
            try {
                db = helper.getWritableDatabase();
                cursor = db.query("rss_feed_favorite", new String[]{"_id"}, "link = ? AND favorite = 1", new String[]{link}, null, null, null);
                return cursor.moveToFirst();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success == null) {
                Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_LONG).show();
            } else if (success) {
                ImageView favoriteView = (ImageView) listItemView.findViewById(R.id.favoriteImage);
                favoriteView.setVisibility(View.VISIBLE);
            }
            db.close();
            cursor.close();
        }
    }

    public class RssFeedsArrayAdapter extends ArrayAdapter {

        private Context context;
        private int layout;
        List<RssItem> rssItems;

        public RssFeedsArrayAdapter(Context context, int layout, List<RssItem> rssItems) {
            super(context, layout, rssItems);
            this.layout = layout;
            this.context = context;
            this.rssItems = rssItems;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);
            ImageView imageSource = (ImageView) convertView.findViewById(R.id.sourceImage);
            TextView textTitle = (TextView) convertView.findViewById(R.id.feedTitleTextView);
            TextView textDate = (TextView) convertView.findViewById(R.id.feedDateTextView);
            RssItem item = rssItems.get(position);
            String channelTitle = item.getChannelTitle();
            File channelImageFile = new File(getActivity().getFilesDir().getAbsolutePath() + "/" + channelTitle + ".png");
            if (channelImageFile.exists()) {
                try {
                imageSource.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream(channelImageFile)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                new ImageLoadTask(item.getChannelImageUrl(), imageSource, channelTitle).execute();
            }

            textTitle.setText(item.getTitle());
            textDate.setText(item.getPubDate());
            new GetFavoriteValueForFeedAsyncTask().execute(convertView, item.getLink());
            return convertView;
        }
    }
}
