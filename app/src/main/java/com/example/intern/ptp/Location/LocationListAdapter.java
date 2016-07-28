package com.example.intern.ptp.Location;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.intern.ptp.R;

import java.util.List;

public class LocationListAdapter extends ArrayAdapter<Location> {

    private final Activity context;
    private final List<Location> items;

    public LocationListAdapter(Activity context,
                               List<Location> items) {
        super(context, R.layout.item_locationlist, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ListRow row;
        if (view == null) {
            row = new ListRow(context, null);
        } else {
            row = (ListRow) view;
        }

        row.setTitle(items.get(position).getLabel());
        row.setContent(items.get(position).getCount());

        return row;

    }

    public class ListRow extends RelativeLayout {
        private TextView mTitle;
        private TextView mContent;

        public ListRow(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater.from(context).inflate(R.layout.item_locationlist, this);

            mTitle = (TextView) findViewById(R.id.title);
            mContent = (TextView) findViewById(R.id.content);
        }

        public void setTitle(String text) {
            mTitle.setText(text);
        }

        public void setContent(String content) {
            mContent.setText(content);
        }
    }
}