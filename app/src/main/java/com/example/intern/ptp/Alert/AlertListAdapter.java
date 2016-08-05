package com.example.intern.ptp.Alert;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.intern.ptp.R;

import java.util.List;

public class AlertListAdapter extends ArrayAdapter<Alert> {
    private final Activity activity;
    private final List<Alert> items;

    public AlertListAdapter(Activity activity,
                            List<Alert> items) {
        super(activity, R.layout.item_alertlist, items);
        this.activity = activity;
        this.items = items;
    }

    /**
     * define layout for each item in the list view
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ListRow row;
        if (view == null) {
            row = new ListRow(activity, null);
        } else {
            row = (ListRow) view;
        }

        // status that whether the alert has been taken care of or not
        boolean ok = !items.get(position).getOk().equalsIgnoreCase("0");

        // get alert icon from resource as a Drawable object
        Drawable drawable = activity.getDrawable(R.drawable.ic_sos);

        // set alert icon for the item
        row.setImage(drawable);

        // set background color for the item depending on the above takencare status
        row.setImageBackgroundColor(ok ? Color.BLUE : Color.RED);

        // set title for the item
        row.setTitle("Resident - " + items.get(position).getFirstname());

        // set content for the item
        row.setContent("Resident " + items.get(position).getFirstname() + (ok ? " has been taken care of." : " needs you help now!"));

        // set color for the content depending on the above takencare status
        row.setContentColor(ok ? Color.BLUE : Color.RED);

        return row;

    }

    /**
     * make the list view more smoothly when scrolling
     */
    public class ListRow extends RelativeLayout {
        private ImageView mImage;
        private TextView mTitle;
        private TextView mContent;

        public ListRow(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater.from(context).inflate(R.layout.item_alertlist, this);

            mImage = (ImageView) findViewById(R.id.image);
            mTitle = (TextView) findViewById(R.id.title);
            mContent = (TextView) findViewById(R.id.content);
        }

        public void setImage(Drawable drawable) {
            mImage.setImageDrawable(drawable);
        }

        public void setImageBackgroundColor(int color) {
            mImage.setBackgroundColor(color);
        }

        public void setTitle(String text) {
            mTitle.setText(text);
        }

        public void setContent(String content) {
            mContent.setText(content);
        }

        public void setContentColor(int color) {
            mContent.setTextColor(color);
        }

        public void clear() {
            mImage.setImageDrawable(null);
        }
    }
}
