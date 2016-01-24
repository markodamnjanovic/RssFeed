package com.markod.rssfeed;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {

    ArrayList<NameDescriptionListItem> objectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.menu_about);

        String versionName = "Version " + BuildConfig.VERSION_NAME;
        NameDescriptionListItem appNameItem = new NameDescriptionListItem(getResources().getString(R.string.app_name), versionName);
        NameDescriptionListItem authorNameItem = new NameDescriptionListItem(getString(R.string.app_author_name), getString(R.string.app_author_email));
        objectList.add(appNameItem);
        objectList.add(authorNameItem);
        NameDescriptionArayAdapter nameDescriptionArayAdapter = new NameDescriptionArayAdapter(this, android.R.layout.simple_list_item_2, objectList);
        ListView aboutListView = (ListView) findViewById(R.id.about_list_view);
        aboutListView.setAdapter(nameDescriptionArayAdapter);

    }
}

class NameDescriptionArayAdapter extends ArrayAdapter<NameDescriptionListItem> {
    private Context context;
    private List<NameDescriptionListItem> objects;

    public NameDescriptionArayAdapter(Context context, int resource, List<NameDescriptionListItem> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(android.R.layout.simple_list_item_2, null);
        NameDescriptionListItem currentObject = objects.get(position);
        TextView nameView = (TextView) view.findViewById(android.R.id.text1);
        TextView descriptionView = (TextView) view.findViewById(android.R.id.text2);
        nameView.setText(currentObject.getItemName());
        descriptionView.setText(currentObject.getItemDescription());
        return view;
    }
}

class NameDescriptionListItem {
    String itemName;
    String itemDescription;

    NameDescriptionListItem(String name, String description) {
        this.itemName = name;
        this.itemDescription = description;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemDescription() {
        return this.itemDescription;
    }
}

