package com.markod.rssfeed.fragments;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class Tab2Fragment extends ListFragment {

    private List<RssItem> rssItems = new ArrayList<>();

    ListView listViewFavoriteFeed;
    public static ArrayList<String> listItemsUniqueIds = new ArrayList<>();
    ActionMode actionMode;

    public Tab2Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_tab2, container, false);
        listViewFavoriteFeed = (ListView) view.findViewById(android.R.id.list);
        new GetFavoriteFeedsAsyncTask().execute(getActivity());
        return view;
    }

    private class GetFavoriteFeedsAsyncTask extends AsyncTask<Activity, Integer, List<RssItem>> {

        @Override
        protected List<RssItem> doInBackground(Activity... activities) {
            Activity activity = activities[0];
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(activity);
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor cursor = db.query("rss_feed_favorite", new String[]{"_id", "link", "feed_title", "pub_date" ,"description","channel_title"}, "favorite = ?", new String[] {Integer.toString(1)}, null, null, null);
                rssItems.clear();
                while (cursor.moveToNext()) {
                    String link = cursor.getString(1);
                    String title = cursor.getString(2);
                    String pubDate = cursor.getString(3);
                    String description = cursor.getString(4);
                    String channelTitle = cursor.getString(5);
                    rssItems.add(new RssItem(title, pubDate, link, channelTitle, description));
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
            listViewFavoriteFeed.setAdapter(adapter);
            //adapter.notifyDataSetChanged();
            //view.setRefreshing(false);
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
            }
            textTitle.setText(item.getTitle());
            textDate.setText(item.getPubDate());
            return convertView;
        }
    }

}
