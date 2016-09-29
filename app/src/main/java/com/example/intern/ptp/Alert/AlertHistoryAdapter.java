package com.example.intern.ptp.Alert;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.FontManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertHistoryAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private final SimpleDateFormat dateParser;

    private List<Alert> items;
    private List<String> alertTypes;

    public AlertHistoryAdapter(Context context, List<Alert> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.alertTypes = Arrays.asList(context.getResources().getStringArray(R.array.alert_types));
        this.dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
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
            view = inflater.inflate(R.layout.item_alert_history, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);

            holder.tookCareOfIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
            holder.locationIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
            holder.timeIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
        }

        Alert alert = items.get(position);

        holder.alertType.setText(alertTypes.get(Integer.parseInt(alert.getType())));

        if (alert.getUsername() != null) {
            holder.tookCareOfIcon.setVisibility(View.VISIBLE);
            holder.tookCareOf.setVisibility(View.VISIBLE);
            holder.tookCareOf.setText(alert.getUsername());
        } else {
            holder.tookCareOfIcon.setVisibility(View.GONE);
            holder.tookCareOf.setVisibility(View.GONE);
        }

        holder.tookCareOf.setText(alert.getUsername());
        holder.location.setText(alert.getLastPositionLabel());

        CharSequence timeSinceAlarm;

        try {
            Date createdAt = dateParser.parse(alert.getCreatedAt());
            timeSinceAlarm = DateUtils.getRelativeTimeSpanString(createdAt.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
        } catch (ParseException ex) {
            timeSinceAlarm = alert.getCreatedAt();
        }

        holder.time.setText(timeSinceAlarm);

        return view;
    }

    public void updateAlerts(List<Alert> alerts) {
        items = alerts;
        notifyDataSetChanged();
    }


    static class ViewHolder {
        @BindView(R.id.alerthistory_type)
        TextView alertType;
        @BindView(R.id.alerthistory_took_care_of_icon)
        TextView tookCareOfIcon;
        @BindView(R.id.alerthistory_took_care_of)
        TextView tookCareOf;
        @BindView(R.id.alerthistory_location_icon)
        TextView locationIcon;
        @BindView(R.id.alerthistory_location)
        TextView location;
        @BindView(R.id.alerthistory_time_icon)
        TextView timeIcon;
        @BindView(R.id.alerthistory_time)
        TextView time;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
