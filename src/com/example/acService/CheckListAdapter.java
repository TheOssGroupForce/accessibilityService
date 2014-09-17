package com.example.acService;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by res on 13/9/14.
 */
public class CheckListAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> data;

    ArrayList<String> checkedFields;
    int checkedCount;
    int maxChecks;

    public CheckListAdapter(Context context, ArrayList<String> data, int maxChecks) {

        if(data == null)
            Log.d("ACSERVICE", "DAta is null");

        this.context = context;
        this.data = data;
        checkedFields = new ArrayList<String>();
        checkedCount = 0;
        this.maxChecks = maxChecks;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int getCheckedItemPositionById(String id){

        for(int i = 0; i< checkedFields.size(); i++){
            if(checkedFields.get(i).equals(id))
                return i;
        }
        return -1;
    }

    public ArrayList<String> getCheckedItems(){
        return checkedFields;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        final ViewHolderItem viewHolder;


        // The convertView argument is essentially a "ScrapView" as described is Lucas post
        // http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
        // It will have a non-null value when ListView is asking you recycle the row layout.
        // So, when convertView is not null, you should simply update its contents instead of inflating a new row    layout.

        if(view==null){

            // inflate the layout
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.checked_list_row, viewGroup, false);

            // well set up the ViewHolder
            viewHolder = new ViewHolderItem();
            viewHolder.checkBoxItem = (CheckBox) view.findViewById(R.id.checkBox);

            // store the holder with the view.
            view.setTag(viewHolder);

        }else{
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolderItem) view.getTag();
        }

        final String item = data.get(i);

        if(item != null){
            viewHolder.checkBoxItem.setText(item);
        }

        viewHolder.checkBoxItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(checkedCount == maxChecks &&  b){
                    viewHolder.checkBoxItem.setChecked(false);
                    Toast.makeText(context, "Only "+ maxChecks+ " fields can be selected", Toast.LENGTH_SHORT).show();
                }
                else if (checkedCount < maxChecks && b){
                    checkedFields.add(item);
                    checkedCount++;
                }
                else if (checkedCount <= maxChecks && !b){
                    int pos = getCheckedItemPositionById(item);
                    if(pos != -1)
                        checkedFields.remove(pos);
                    checkedCount--;
                }
            }
        });

        return view;
    }

    static class ViewHolderItem {
        CheckBox checkBoxItem;
    }
}
