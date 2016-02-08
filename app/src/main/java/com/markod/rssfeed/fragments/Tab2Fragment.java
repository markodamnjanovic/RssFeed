package com.markod.rssfeed.fragments;


import android.app.Activity;
import android.content.ContentValues;
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
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
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
public class Tab2Fragment extends ListFragment {

    private List<RssItem> rssItems = new ArrayList<>();

    ListView listViewFavoriteFeed;
    public static ArrayList<String> listItemsUniqueIds = new ArrayList<>();
    ActionMode actionMode;
    RssFeedsArrayAdapter adapter;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listViewFavoriteFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        listViewFavoriteFeed.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listViewFavoriteFeed.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            SQLiteDatabase db;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle(listViewFavoriteFeed.getCheckedItemCount() + " selected");
                View listItemView = listViewFavoriteFeed.getChildAt(position);
                ImageView checkView = (ImageView) listItemView.findViewById(R.id.imageCheck);
                ImageView feedImageView = (ImageView) listItemView.findViewById(R.id.sourceImage);
                if (checked) {
                    feedImageView.setVisibility(View.INVISIBLE);
                    checkView.setVisibility(View.VISIBLE);
                } else {
                    feedImageView.setVisibility(View.VISIBLE);
                    checkView.setVisibility(View.INVISIBLE);
                }

                adapter.toggleSelection(position + 1);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.action_mode, menu);
                actionMode = mode;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        RssFeedDatabaseHelper helper = new RssFeedDatabaseHelper(getActivity());
                        try {
                            db = helper.getWritableDatabase();
                            ArrayList itemIdsToDelete = adapter.getSelectedIds();
                            ArrayList<String> uniqueIdsToDelete = new ArrayList<>();
                            for (int i = 0; i < listItemsUniqueIds.size(); i++) {
                                if (itemIdsToDelete.contains(new Integer(i))) {
                                    uniqueIdsToDelete.add(listItemsUniqueIds.get(i));
                                }
                            }
                            for (String uniqueId : uniqueIdsToDelete) {
                                ContentValues values = new ContentValues();
                                values.put("favorite", 0);
                                db.update("rss_feed_favorite", values, "unique_id = ?", new String[]{uniqueId});
                            }
                            mode.finish();
                            Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_LONG).show();
                            return true;
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_LONG).show();
                        }

                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (db != null) db.close();
                listViewFavoriteFeed = (ListView) getView().findViewById(android.R.id.list);
                new GetFavoriteFeedsAsyncTask().execute(getActivity());
            }
        });
    }

    private class GetFavoriteFeedsAsyncTask extends AsyncTask<Activity, Integer, List<RssItem>> {

        @Override
        protected List<RssItem> doInBackground(Activity... activities) {
            Activity activity = activities[0];
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(activity);
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                Cursor cursor = db.query("rss_feed_favorite", new String[]{"_id", "unique_id", "link", "feed_title", "pub_date" ,"description","channel_title"}, "favorite = ?", new String[] {Integer.toString(1)}, null, null, null);
                rssItems.clear();
                listItemsUniqueIds.clear();
                while (cursor.moveToNext()) {
                    listItemsUniqueIds.add(cursor.getString(1));
                    String link = cursor.getString(2);
                    String title = cursor.getString(3);
                    String pubDate = cursor.getString(4);
                    String description = cursor.getString(5);
                    String channelTitle = cursor.getString(6);
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
            adapter = new RssFeedsArrayAdapter(getActivity(), R.layout.rss_feed_list_item, rssItems);
            listViewFavoriteFeed.setAdapter(adapter);
        }
    }

    public class RssFeedsArrayAdapter extends ArrayAdapter {

        private Context context;
        private int layout;
        List<RssItem> rssItems;
        ArrayList<Integer> selectedItemsIds = new ArrayList<Integer>();

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

        public void toggleSelection(int position) {
            if (!selectedItemsIds.contains(position - 1))
                selectedItemsIds.add(position - 1);
            else
                selectedItemsIds.remove(new Integer(position - 1));
        }

        public ArrayList<Integer> getSelectedIds() {
            return selectedItemsIds;
        }
    }

}
