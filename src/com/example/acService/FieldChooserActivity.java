package com.example.acService;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by res on 12/9/14.
 */
public class FieldChooserActivity extends Activity {

    private ListView list1, list2;
    private Button btnDone;

    CheckListAdapter adapter1, adapter2;
    private ArrayList<String> data1,data2, arr1, arr2;
    private String selectedField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fields_select_main);

        Bundle extras = getIntent().getExtras();
        data1 = new ArrayList<String>();
        data1.clear();
        data1.addAll(extras.getStringArrayList("input_fields"));

        if(data1.isEmpty()){
            Log.d("NULLL", "NULLLLLLLL");
        }
        data2 = new ArrayList<String>();
        data2.clear();
        data2.addAll(extras.getStringArrayList("buttons"));

        adapter1 = new CheckListAdapter(getApplicationContext(), data1, 2);
        adapter2 = new CheckListAdapter(getApplicationContext(), data2, 1);

        list1 = (ListView) findViewById(R.id.listView1);
        list1.setAdapter(adapter1);

        list2 = (ListView) findViewById(R.id.listView2);
        list2.setAdapter(adapter2);

        btnDone = (Button) findViewById(R.id.done_btn);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arr1 = new ArrayList<String>();
                arr2 = new ArrayList<String>();
                arr1 = adapter1.getCheckedItems();
                arr2 = adapter2.getCheckedItems();

                finish();
            }
        });
    }




    @Override
    public void finish() {
        // Prepare data intent
        Intent data = new Intent();
        data.putExtra("input_fields", arr1);
        data.putExtra("button", arr2);
        // Activity finished ok, return the data
        setResult(RESULT_OK, data);
        super.finish();
    }



}