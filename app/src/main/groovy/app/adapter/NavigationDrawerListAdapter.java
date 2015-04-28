package app.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import app.R;

public class NavigationDrawerListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private String[] values;
    private TypedArray icons;

    public NavigationDrawerListAdapter(Context context, String[] values, TypedArray icons) {
        super(context, R.layout.list_item_drawer, values);
        this.context = context;
        this.values = values;
        this.icons = icons;
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
            rowView = inflater.inflate(R.layout.list_item_drawer, parent, false);
            holder = new NavigationDrawerListAdapter.ViewHolder();
            holder.title = (TextView) rowView.findViewById(R.id.text);
            holder.icon = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(holder);
        } else {
            holder = (NavigationDrawerListAdapter.ViewHolder) rowView.getTag();
        }

        holder.title.setText(values[position]);
        holder.icon.setImageResource(icons.getResourceId(position, -1));

        return rowView;
    }

}