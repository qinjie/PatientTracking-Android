package com.example.intern.ptp.views.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.network.models.Location;
import com.example.intern.ptp.utils.FontManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MapListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Location> locations;

    public MapListAdapter(Context context, List<Location> locations) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.locations = locations;
    }

    @Override
    public int getCount() {
        return locations.size();
    }

    @Override
    public Object getItem(int position) {
        return locations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.item_maps, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);

            holder.alertIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
            holder.userIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
        }

        Location item = locations.get(position);
        holder.title.setText(item.getLabel());
        holder.residentCount.setText(item.getCount());

        if (item.getOngoingAlerts() > 0) {
            holder.alertIcon.setTextColor(ContextCompat.getColor(context, R.color.red));
        } else {
            holder.alertIcon.setTextColor(ContextCompat.getColor(context, R.color.light_grey));
        }

        return view;
    }

    public void updateLocations(List<Location> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @BindView(R.id.maps_title)
        TextView title;
        @BindView(R.id.maps_alert_icon)
        TextView alertIcon;
        @BindView(R.id.maps_user_icon)
        TextView userIcon;
        @BindView(R.id.maps_resident_count)
        TextView residentCount;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
