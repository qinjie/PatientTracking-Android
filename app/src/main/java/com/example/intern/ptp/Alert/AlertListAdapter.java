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
    private final Activity context;
    private final List<Alert> items;

    public AlertListAdapter(Activity context,
                            List<Alert> items) {
        super(context, R.layout.item_alertlist, items);
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

        boolean ok = !items.get(position).getOk().equalsIgnoreCase("0");
        Drawable drawable = context.getDrawable(R.drawable.ic_sos);
        row.setImage(drawable);
        row.setImageBackgroundColor(ok ? Color.BLUE : Color.RED);

        row.setTitle("PTP Alert " + items.get(position).getResidentId());

        row.setContent("Resident " + items.get(position).getFirstname() + (ok ? " has been taken care of." : " needs you help now!"));
        row.setContentColor(ok ? Color.BLUE : Color.RED);

        return row;

    }

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
    }
}
