package com.termproject.metaverse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter implements Filterable {

    private ArrayList<ListItem> listItems = new ArrayList<ListItem>();
    //필터링된 결과 데이터 저장을 위한 ArrayList
    private ArrayList<ListItem> filteredItems = listItems;

    Filter filter;

    int count = 0;

    public ListItemAdapter() {

    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup vg) {
        final Context context = vg.getContext();

        if(view == null) {
            LayoutInflater inflater
                    = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, vg, false);
        }

        ImageView profile = (ImageView)view.findViewById(R.id.profile);
        TextView name = (TextView)view.findViewById(R.id.name);

        ListItem listItem = filteredItems.get(position);

        profile.setImageDrawable(listItem.getImg());
        name.setText(listItem.getTxt1());

        return view;
    }

    public void addItem(Drawable d, String str1) {
        ListItem item = new ListItem();

        item.setImg(d);
        item.setTxt1(str1);

        listItems.add(item);
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ListFilter();
        }
        return filter;
    }

    private class ListFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence cs) {
            FilterResults results = new FilterResults();

            if(cs == null || cs.length() == 0) {
                results.values = listItems;
                results.count = listItems.size();
            } else {
                ArrayList<ListItem> itemList = new ArrayList<ListItem>();
                for(ListItem item : listItems) {
                    if (item.getTxt1().toUpperCase().contains(cs.toString().toUpperCase())) {
                        itemList.add(item);
                    }
                }
                results.values = itemList;
                results.count = itemList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence cs, FilterResults results) {

            filteredItems = (ArrayList<ListItem>) results.values;

            if(results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }


}
