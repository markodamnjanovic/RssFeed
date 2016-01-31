package com.markod.rssfeed;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.UUID;

public class AddEditSourceActivity extends AppCompatActivity {

    Intent intent;
    Boolean isAddExtra;
    SQLiteDatabase db;
    android.support.v7.app.ActionBar actionBar;

    private EditText sourceNameView;
    private EditText sourceUrlView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_source);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sourceNameView = (EditText) findViewById(R.id.source_name_edit);
        sourceUrlView = (EditText) findViewById(R.id.source_url_edit);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        intent = getIntent();
        isAddExtra = intent.getBooleanExtra(MainActivity.IS_ADD_EXTRA, true);
        if (isAddExtra) actionBar.setTitle(R.string.AddTitle);
        else actionBar.setTitle(R.string.EditTitle);
        if (!isAddExtra) new GetSourceAsyncTask().execute();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(sourceNameView, InputMethodManager.SHOW_IMPLICIT);
        imm.showSoftInput(sourceUrlView, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_source, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                new SaveSourceAsyncTask().execute();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) db.close();
    }

    private class GetSourceAsyncTask extends AsyncTask<Void, Void, Boolean> {

        int sourceId;
        String sourceName;
        String sourceUrl;

        @Override
        protected void onPreExecute() {
            sourceId = intent.getIntExtra(MainActivity.ITEM_POSITION_EXTRA, 1);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(AddEditSourceActivity.this);
            try {
                db = helper.getWritableDatabase();
                Cursor cursor = db.query("rss_feed", new String[]{"_id", "source_name", "source_url"}, "_id = ?", new String[]{Integer.toString(sourceId)}, null, null, null);
                if (cursor.moveToFirst()) {
                    sourceName = cursor.getString(1);
                    sourceUrl = cursor.getString(2);
                }
                cursor.close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(AddEditSourceActivity.this, "Database unavailable", Toast.LENGTH_LONG).show();
            } else {
                sourceNameView.setText(sourceName);
                sourceUrlView.setText(sourceUrl);
            }
        }
    }

    private class SaveSourceAsyncTask extends AsyncTask<Void, Void, Boolean> {

        ContentValues values;
        String sourceName;
        String sourceUrl;

        @Override
        protected void onPreExecute() {
            String sourceName = sourceNameView.getText().toString();
            String sourceUrl = sourceUrlView.getText().toString();
            if (sourceName.isEmpty()) {
                sourceNameView.setError(getString(R.string.populate_field_error));
                cancel(true);
            } else if (sourceUrl.isEmpty()) {
                sourceUrlView.setError(getString(R.string.populate_field_error));
                cancel(true);
            }
            String uniqueId = UUID.randomUUID().toString();
            values = new ContentValues();
            values.put("unique_id", uniqueId);
            values.put("source_name", sourceName);
            values.put("source_url", sourceUrl);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(AddEditSourceActivity.this);
            try {
                db = helper.getWritableDatabase();
                if (isAddExtra) {
                    values.put("show_feed", 1);
                    db.insert("rss_feed", null, values);
                } else {
                    int itemPosition = intent.getIntExtra(MainActivity.ITEM_POSITION_EXTRA, 0);
                    db.update("rss_feed", values, "_id = ?", new String[]{Integer.toString(itemPosition)});
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(AddEditSourceActivity.this, "Database unavailable", Toast.LENGTH_LONG).show();
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
