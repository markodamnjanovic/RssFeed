package com.markod.rssfeed.fragments;


import android.app.Activity;
import android.app.LauncherActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.markod.rssfeed.R;
import com.markod.rssfeed.RssFeedDatabaseHelper;


public class Tab3Fragment extends ListFragment {

    public Tab3Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_tab3, container, false);
        setCursorAdapter(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setCursorAdapter(getView());
    }

    private void setCursorAdapter(View view) {
        try {
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(getActivity());
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor sourcesCursor = db.query("rss_feed", new String[]{"_id", "source_name", "source_url", "show_feed"}, null, null, null, null, null);
            CursorAdapter cursorAdapter = new SourcesCursorAdapter(getActivity(), R.layout.name_description_switch_list_item, sourcesCursor, new String[]{"source_name", "source_url"}, new int[]{R.id.text1, R.id.text2});
            ListView listViewSources = (ListView) view.findViewById(android.R.id.list);
            listViewSources.setAdapter(cursorAdapter);
            db.close();
        } catch (Exception e) {
            Toast toast = Toast.makeText(getContext(), "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class SourcesCursorAdapter extends SimpleCursorAdapter {

        private Context context;
        private int layout;
        private Cursor cursor;
        private final LayoutInflater inflater;

        public SourcesCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from,to);
            this.layout=layout;
            this.context = context;
            this.inflater=LayoutInflater.from(context);
            this.cursor=c;
        }

        @Override
        public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            Switch switch1 = (Switch) view.findViewById(R.id.switch1);
            Boolean switchEnabled;
            switchEnabled = cursor.getInt(3) == 1;
            switch1.setChecked(switchEnabled);
        }
    }
}
