package com.example.acService;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by res on 11/9/14.
 */
public class ActivityChooserActivity extends ListActivity {

    private String appName;

    ArrayAdapter<String> adapter;
    private ArrayList<String> data;
    private String selectedActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_main);

        Bundle extras = getIntent().getExtras();
        appName = extras.getString("appname");

        data = new ArrayList<String>();
        try {
            getActivityList();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                data);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);


        try {
            selectedActivity = data.get(position);
            finish();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ActivityChooserActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ActivityChooserActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public void finish() {
        // Prepare data intent
        Intent data = new Intent();
        data.putExtra("activity", selectedActivity);
        // Activity finished ok, return the data
        setResult(RESULT_OK, data);
        super.finish();
    }

    private void getActivityList() throws PackageManager.NameNotFoundException {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = this.getPackageManager();
        PackageInfo info = pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);

        ActivityInfo[] list = info.activities;
        List<String> items = new ArrayList<String>();
        for (ActivityInfo item : list) {
            try {
                items.add(item.name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data.clear();
        data.addAll(items);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                data);
        setListAdapter(adapter);
    }
}