package com.markod.rssfeed;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RssFeedDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "rss_feed";
    private static final int DB_VERSION = 1;

    public RssFeedDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE rss_feed (_id INTEGER PRIMARY KEY AUTOINCREMENT, source_name TEXT, source_url TEXT, favorite INTEGER, show_feed INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
