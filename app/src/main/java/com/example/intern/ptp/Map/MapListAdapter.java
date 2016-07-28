package com.example.intern.ptp.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.intern.ptp.Location.Location;
import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class MapListAdapter extends ArrayAdapter<Location> {

    private final Activity activity;
    private final List<Location> items;

    public MapListAdapter(Activity activity,
                          List<Location> items) {
        super(activity, R.layout.item_maplist, items);
        this.activity = activity;
        this.items = items;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ListRow row;
        if (view == null) {
            row = new ListRow(activity, null);
        } else {
            row = (ListRow) view;
        }

        row.setTitle(items.get(position).getLabel());
        String thumbnailUrl = Preferences.imageRoot + items.get(position).getThumbnailPath();
        String imageUrl = Preferences.imageRoot + items.get(position).getFilePath();
        row.setImage(thumbnailUrl, imageUrl);

        return row;
    }

    public class ListRow extends RelativeLayout {
        private TextView mTitle;
        private ImageView mImage;

        public ListRow(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater.from(context).inflate(R.layout.item_maplist, this);

            mTitle = (TextView) findViewById(R.id.title);
            mImage = (ImageView) findViewById(R.id.image);
        }

        public void setTitle(String text) {
            mTitle.setText(text);
        }

        public void setImage(String thumbnailUrl, final String imageUrl) {
            Picasso.with(activity)
                    .load(thumbnailUrl)
                    .priority(Picasso.Priority.HIGH)
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .networkPolicy(NetworkPolicy.NO_STORE)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
//                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.null_thumbnail)
                    .into(mImage);

            Picasso.with(activity)
                    .load(imageUrl)
                    .priority(Picasso.Priority.HIGH)
//                    .networkPolicy(NetworkPolicy.NO_CACHE)
//                    .networkPolicy(NetworkPolicy.NO_STORE)
//                    .memoryPolicy(MemoryPolicy.NO_CACHE)
//                    .memoryPolicy(MemoryPolicy.NO_STORE)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
        }

        public void clear() {
            mImage.setImageDrawable(null);
        }
    }
}