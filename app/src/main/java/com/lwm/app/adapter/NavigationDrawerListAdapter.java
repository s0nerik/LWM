package com.lwm.app.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lwm.app.R;

public class NavigationDrawerListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private String[] values;
    private TypedArray icons;

    public NavigationDrawerListAdapter(Context context, String[] values, TypedArray icons) {
        super(context, R.layout.list_item_drawer, values);
        this.context = context;
        this.values = values;
        this.icons = icons;
//        values = context.getResources().getStringArray(R.array.drawer_items);
//        icons = context.getResources().obtainTypedArray(R.array.drawer_icons);
    }

    static class ViewHolder {
        public TextView title;
        public ImageView icon;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        NavigationDrawerListAdapter.ViewHolder holder;

        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.list_item_drawer, null, true);
            holder = new NavigationDrawerListAdapter.ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.drawer_item_text);
            holder.icon = (ImageView) rowView.findViewById(R.id.drawer_item_icon);
            rowView.setTag(holder);
        } else {
            holder = (NavigationDrawerListAdapter.ViewHolder) rowView.getTag();
        }

        holder.title.setText(values[position]);
        holder.icon.setImageResource(icons.getResourceId(position, -1));

        return rowView;
    }

}