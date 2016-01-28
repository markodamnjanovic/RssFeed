package com.markod.rssfeed.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;
import com.markod.rssfeed.R;
import com.markod.rssfeed.RssFeedDatabaseHelper;
import java.util.ArrayList;

public class Tab3Fragment extends ListFragment {

    Cursor sourcesCursor;
    ListView listViewSources;
    SourcesCursorAdapter cursorAdapter;
    public static ArrayList<String> listItemsUniqueIds = new ArrayList<>();
    ActionMode actionMode;

    public Tab3Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab3, container, false);
        setCursorAdapter(view, R.layout.name_description_switch_list_item);
        return view;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listViewSources.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listViewSources.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            SQLiteDatabase db;
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mode.setTitle(listViewSources.getCheckedItemCount() + " selected");
                View listItemView = listViewSources.getChildAt(position);
                //this here only works on second item click after action mode has started because at the beginning, listItemView is null because of adapter change!
                if (listItemView != null) {
                    ImageView checkView = (ImageView) listItemView.findViewById(R.id.imageCheck);
                    if (checked) checkView.setVisibility(View.VISIBLE);
                    else checkView.setVisibility(View.INVISIBLE);
                }
                cursorAdapter.toggleSelection(position + 1);
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
                cursorAdapter = new SourcesCursorAdapter(getActivity(), R.layout.name_description_checkbox_list_item, sourcesCursor, new String[]{"source_name", "source_url"}, new int[]{R.id.text1, R.id.text2});
                listViewSources.setAdapter(cursorAdapter);

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        RssFeedDatabaseHelper helper = new RssFeedDatabaseHelper(getActivity());
                        try {
                            db = helper.getWritableDatabase();
                            ArrayList itemIdsToDelete = cursorAdapter.getSelectedIds();
                            ArrayList<String> uniqueIdsToDelete = new ArrayList<String>();
                            for (int i = 0; i < listItemsUniqueIds.size(); i++) {
                                if (itemIdsToDelete.contains(new Integer(i))) {
                                    uniqueIdsToDelete.add(listItemsUniqueIds.get(i));
                                }
                            }
                            for (String uniqueId : uniqueIdsToDelete) {
                                db.delete("rss_feed", "unique_id = ?", new String[]{uniqueId});
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
                setCursorAdapter(getView(), R.layout.name_description_switch_list_item);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setCursorAdapter(getView(), R.layout.name_description_switch_list_item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sourcesCursor != null)
            sourcesCursor.close();
    }

    @Override
    public void onStop() {
        super.onStop();
        //this only executes when user goes from 3. to 1. tab, if user goes to 3. to 2. tab, nothing happens
        if (actionMode != null) actionMode.finish();
    }

    private void setCursorAdapter(View view, int layoutId) {
        listViewSources = (ListView) view.findViewById(android.R.id.list);
        new GetSourcesAsyncTask().execute(layoutId);
    }

    private class SourcesCursorAdapter extends SimpleCursorAdapter {

        private Context context;
        private int layout;
        private Cursor cursor;
        private final LayoutInflater inflater;
        ArrayList<Integer> selectedItemsIds = new ArrayList<Integer>();

        public SourcesCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            this.layout = layout;
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.cursor = c;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            Switch switch1 = (Switch) view.findViewById(R.id.switch1);
            if (switch1 != null) {
                Boolean switchEnabled;
                switchEnabled = cursor.getInt(4) == 1;
                switch1.setChecked(switchEnabled);
            } else {
                view.findViewById(R.id.imageCheck).setVisibility(View.INVISIBLE);
            }
        }

        public void toggleSelection(int position) {;
            if (!selectedItemsIds.contains(position - 1))
                selectedItemsIds.add(position - 1);
            else
                selectedItemsIds.remove(new Integer(position - 1));
        }

        public ArrayList<Integer> getSelectedIds() {
            return selectedItemsIds;
        }
    }

    private class GetSourcesAsyncTask extends AsyncTask<Integer, Void, Boolean> {

        CursorAdapter cursorAdapter;
        SQLiteDatabase db;
        Integer layoutId;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Integer... layoutIds) {
            layoutId = layoutIds[0];
            RssFeedDatabaseHelper helper = new RssFeedDatabaseHelper(getActivity());
            try {
                db = helper.getReadableDatabase();
                sourcesCursor = db.query("rss_feed", new String[]{"_id", "unique_id", "source_name", "source_url", "show_feed"}, null, null, null, null, null);
                listItemsUniqueIds.clear();
                if (sourcesCursor.moveToFirst()) {
                    listItemsUniqueIds.add(sourcesCursor.getString(1));
                    while (sourcesCursor.moveToNext()) {
                        listItemsUniqueIds.add(sourcesCursor.getString(1));
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(getActivity(), "Database unavailable", Toast.LENGTH_LONG).show();
            } else {
                cursorAdapter = new SourcesCursorAdapter(getActivity(), layoutId, sourcesCursor, new String[]{"source_name", "source_url"}, new int[]{R.id.text1, R.id.text2});
                listViewSources.setAdapter(cursorAdapter);
                db.close();
            }
        }
    }
}
