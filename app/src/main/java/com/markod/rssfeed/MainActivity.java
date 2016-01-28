package com.markod.rssfeed;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import com.markod.rssfeed.fragments.Tab1Fragment;
import com.markod.rssfeed.fragments.Tab2Fragment;
import com.markod.rssfeed.fragments.Tab3Fragment;

public class MainActivity extends AppCompatActivity {

    static final int ADD_EDIT_SOURCE_REQUEST = 1;
    static final String ITEM_POSITION_EXTRA = "itemPositionExtra";
    static final String IS_ADD_EXTRA = "isAddExtra";

    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout.addTab(tabLayout.newTab().setText("Feeds").setIcon(R.drawable.ic_feeds));
        tabLayout.addTab(tabLayout.newTab().setText("Favorites").setIcon(R.drawable.ic_star_rate_black_18dp));
        tabLayout.addTab(tabLayout.newTab().setText("Sources").setIcon(R.drawable.ic_settings_black_24dp));
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), 3);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                invalidateOptionsMenu();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean sourcesTabSelected = tabLayout.getSelectedTabPosition() == 2;
        menu.findItem(R.id.menu_add).setVisible(sourcesTabSelected);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_add:
                intent = new Intent(this, AddEditSourceActivity.class);
                intent.putExtra(IS_ADD_EXTRA, true);
                startActivityForResult(intent, ADD_EDIT_SOURCE_REQUEST);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onEditArrowClicked(View view) {
        View listItem = (View) view.getParent();
        ListView listView = (ListView) listItem.getParent();
        final int position = listView.getPositionForView(listItem);
        Intent intent = new Intent(this, AddEditSourceActivity.class);
        intent.putExtra(IS_ADD_EXTRA, false);
        intent.putExtra(ITEM_POSITION_EXTRA, position + 1);
        startActivityForResult(intent, ADD_EDIT_SOURCE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_EDIT_SOURCE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Source saved successfully", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onSwitchClicked(View view) {
        View listItem = (View) view.getParent();
        ListView listView = (ListView) listItem.getParent();
        final int position = listView.getPositionForView(listItem);
        String listItemUniqueId = Tab3Fragment.listItemsUniqueIds.get(position);
        boolean switchOn = ((Switch) view).isChecked();
        new UpdateSourceAsyncTask().execute(listItemUniqueId, switchOn);
    }

    private class UpdateSourceAsyncTask extends AsyncTask<Object, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Object... objects) {
            String sourceUniqueId = (String) objects[0];
            boolean switchOn = (boolean) objects[1];
            SQLiteOpenHelper helper = new RssFeedDatabaseHelper(MainActivity.this);
            try {
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues values = new ContentValues();
                if (switchOn) values.put("show_feed", 1);
                else values.put("show_feed", 0);
                db.update("rss_feed", values, "unique_id = ?", new String[]{sourceUniqueId});
                db.close();
                return true;
            } catch (SQLiteException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (!success) {
                Toast.makeText(MainActivity.this, "Database unavailable", Toast.LENGTH_LONG).show();
            }
        }
    }
}


class PagerAdapter extends FragmentPagerAdapter {

    int tabsNumber;

    public PagerAdapter(FragmentManager fragmentManager, int tabsNumber) {
        super(fragmentManager);
        this.tabsNumber = tabsNumber;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Tab1Fragment tab1 = new Tab1Fragment();
                return tab1;
            case 1:
                Tab2Fragment tab2 = new Tab2Fragment();
                return tab2;
            case 2:
                Tab3Fragment tab3 = new Tab3Fragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabsNumber;
    }
}
