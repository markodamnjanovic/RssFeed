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

public class AddEditSourceActivity extends AppCompatActivity {

    Intent intent;
    Boolean isAddExtra;
    SQLiteDatabase db;

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
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        intent = getIntent();
        isAddExtra = intent.getBooleanExtra(MainActivity.IS_ADD_EXTRA, true);
        int sourceId = intent.getIntExtra(MainActivity.ITEM_POSITION_EXTRA, 1);
        try {
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(this);
            db = helper.getWritableDatabase();
            if (!isAddExtra) {
                Cursor cursor = db.query("rss_feed", new String[]{"_id", "source_name", "source_url"}, "_id = ?", new String[]{Integer.toString(sourceId)}, null, null, null);
                if (cursor.moveToFirst()) {
                    String sourceName = cursor.getString(1);
                    String sourceUrl = cursor.getString(2);
                    sourceNameView.setText(sourceName);
                    sourceUrlView.setText(sourceUrl);
                }
                cursor.close();
                actionBar.setTitle(R.string.EditTitle);
            } else actionBar.setTitle(R.string.AddTitle);
        } catch (Exception e) {
            Toast.makeText(this, "Database unavailable", Toast.LENGTH_LONG).show();
        }
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
                String sourceName = sourceNameView.getText().toString();
                String sourceUrl = sourceUrlView.getText().toString();
                if (sourceName.isEmpty()) {
                    sourceNameView.setError(getString(R.string.populate_field_error));
                    break;
                } else if (sourceUrl.isEmpty()) {
                    sourceUrlView.setError(getString(R.string.populate_field_error));
                    break;
                }
                ContentValues values = new ContentValues();
                values.put("source_name", sourceName);
                values.put("source_url", sourceUrl);
                if (isAddExtra) {
                    values.put("favorite", 0);
                    values.put("show_feed", 1);
                    db.insert("rss_feed", null, values);
                } else {
                    int itemPosition = intent.getIntExtra(MainActivity.ITEM_POSITION_EXTRA, 0);
                    db.update("rss_feed", values, "_id = ?", new String[]{Integer.toString(itemPosition)});
                }
                setResult(RESULT_OK);
                finish();
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
}
