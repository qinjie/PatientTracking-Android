package com.example.intern.ptp.Alert;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.FontManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertListAdapter extends BaseAdapter {

    // TODO: REMOVE THIS. JUST SEPT2016 DEMO
    private static final int MAX_RESIDENTS = 4;
    private static int residentNr = 0;
    private HashMap<String, String> residentProfilePics;

    private final LayoutInflater inflater;
    private final SimpleDateFormat dateParser;

    private Context context;
    private List<Alert> ongoingAlerts;
    private List<Alert> solvedAlerts;

    private static final int ONGOING_LABEL_ITEM_COUNT = 1;
    private static final int SOLVED_LABEL_ITEM_COUNT = 1;

    private static final int VIEW_TYPE_LABEL = 0;
    private static final int VIEW_TYPE_ALERT = 1;

    public AlertListAdapter(Context context, List<Alert> alerts) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        ongoingAlerts = new ArrayList<>(alerts.size());
        solvedAlerts = new ArrayList<>(alerts.size());

        dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        residentProfilePics = new HashMap<>();

        saveAlertsSorted(alerts);
    }

    @Override
    public int getCount() {
        int count = 0;

        if (ongoingAlerts.size() > 0) {
            count += ONGOING_LABEL_ITEM_COUNT + ongoingAlerts.size();
        }

        if (solvedAlerts.size() > 0) {
            count += SOLVED_LABEL_ITEM_COUNT + solvedAlerts.size();
        }

        return count;
    }

    @Override
    public Object getItem(int position) {
        int arrIndex = position;

        if (ongoingAlerts.size() > 0) {
            arrIndex -= ONGOING_LABEL_ITEM_COUNT;

            if (arrIndex < ongoingAlerts.size()) {
                return ongoingAlerts.get(arrIndex);
            } else {
                arrIndex -= ongoingAlerts.size();
            }
        }

        if (solvedAlerts.size() > 0) {
            arrIndex -= SOLVED_LABEL_ITEM_COUNT;

            if (arrIndex < solvedAlerts.size()) {
                return solvedAlerts.get(arrIndex);
            }
        }

        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_LABEL;
        } else if (ongoingAlerts.size() > 0 && position == ONGOING_LABEL_ITEM_COUNT + ongoingAlerts.size()) {
            return VIEW_TYPE_LABEL;
        } else {
            return VIEW_TYPE_ALERT;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
        Resources res = context.getResources();

        switch (getItemViewType(position)) {
            case VIEW_TYPE_LABEL: {
                LabelViewHolder holder;

                if (view != null && view.getTag() instanceof LabelViewHolder) {
                    holder = (LabelViewHolder) view.getTag();

                } else {
                    view = inflater.inflate(R.layout.item_alerts_label, parent, false);
                    holder = new LabelViewHolder(view);
                    view.setTag(holder);
                    view.setEnabled(false);
                    view.setOnClickListener(null);
                    holder.icon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                }

                if (position == 0 && ongoingAlerts.size() > 0) {
                    holder.icon.setText(    res.getString(R.string.fa_bell));
                    holder.icon.setTextColor(ContextCompat.getColor(context, R.color.red));

                    String text = String.format(Locale.getDefault(), res.getString(R.string.alerts_alert_ongoing_alerts), ongoingAlerts.size());
                    holder.text.setText(text);
                } else {
                    holder.icon.setText(res.getString(R.string.fa_check_square));
                    holder.icon.setTextColor(ContextCompat.getColor(context, R.color.green));

                    String text = String.format(Locale.getDefault(), res.getString(R.string.alerts_alert_solved_alerts), solvedAlerts.size());
                    holder.text.setText(text);
                }

                break;
            }

            case VIEW_TYPE_ALERT: {
                AlertViewHolder holder;

                if (view != null && view.getTag() instanceof AlertViewHolder) {
                    holder = (AlertViewHolder) view.getTag();
                } else {
                    view = inflater.inflate(R.layout.item_alerts_alert, parent, false);
                    holder = new AlertViewHolder(view);
                    view.setTag(holder);
                    holder.tookCareOfIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                    holder.locationIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                    holder.timeIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                }

                Alert alert = (Alert) getItem(position);

                String title = String.format(Locale.getDefault(), res.getString(R.string.alert_title), alert.getFirstname(), alert.getLastname());
                holder.title.setText(title);

                if(alert.getUsername() != null) {
                    holder.tookCareOfIcon.setVisibility(View.VISIBLE);
                    holder.tookCareOf.setVisibility(View.VISIBLE);
                    holder.tookCareOf.setText(alert.getUsername());
                } else {
                    holder.tookCareOfIcon.setVisibility(View.GONE);
                    holder.tookCareOf.setVisibility(View.GONE);
                }

                holder.location.setText(alert.getLastPositionLabel());

                CharSequence timeSinceAlarm;

                try {
                    Date createdAt = dateParser.parse(alert.getCreatedAt());
                    timeSinceAlarm = DateUtils.getRelativeTimeSpanString(createdAt.getTime(), new Date().getTime(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL);
                } catch (ParseException ex) {
                    timeSinceAlarm = alert.getCreatedAt();
                }

                holder.time.setText(timeSinceAlarm);

                if (!alert.getOk().equalsIgnoreCase("0")) {
                    holder.takeCareButton.setVisibility(View.INVISIBLE);
                } else {
                    holder.takeCareButton.setVisibility(View.VISIBLE);
                }

                //TODO: REMOVE THIS. IS JUST FOR 2016 SEPT DEMO
                String profilePicture = residentProfilePics.get(alert.getResidentId());

                if(profilePicture == null) {
                    profilePicture = "resident" + (residentNr % MAX_RESIDENTS);
                    residentProfilePics.put(alert.getResidentId(), profilePicture);
                    residentNr++;
                }

                Drawable image = context.getDrawable(context.getResources().getIdentifier(profilePicture, "drawable", context.getPackageName()));
                holder.profilePicture.setImageDrawable(image);
                // TODO END

                holder.takeCareButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        ((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
                    }
                });
            }
        }

        return view;
    }

    public void updateAlerts(List<Alert> alerts){
        ongoingAlerts.clear();
        solvedAlerts.clear();

        saveAlertsSorted(alerts);
        notifyDataSetChanged();
    }

    public void updateAlerts() {
        List<Alert> alerts = new ArrayList<>(solvedAlerts.size() + ongoingAlerts.size());

        alerts.addAll(ongoingAlerts);
        alerts.addAll(solvedAlerts);
        ongoingAlerts.clear();
        solvedAlerts.clear();

        saveAlertsSorted(alerts);
        notifyDataSetChanged();
    }

    private void saveAlertsSorted(List<Alert> alerts) {

        for (Alert alert : alerts) {
            boolean isSolved = !alert.getOk().equalsIgnoreCase("0");

            if (isSolved) {
                solvedAlerts.add(alert);
            } else {
                ongoingAlerts.add(alert);
            }
        }
    }

    static class LabelViewHolder {
        @BindView(R.id.alert_label_icon)
        TextView icon;
        @BindView(R.id.alert_label_text)
        TextView text;

        public LabelViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class AlertViewHolder {
        @BindView(R.id.alerts_alert_profile_picture)
        ImageView profilePicture;
        @BindView(R.id.alerts_alert_title)
        TextView title;
        @BindView(R.id.alert_alert_took_care_by_icon)
        TextView tookCareOfIcon;
        @BindView(R.id.alert_alert_took_care_by)
        TextView tookCareOf;
        @BindView(R.id.alerts_alert_location_icon)
        TextView locationIcon;
        @BindView(R.id.alerts_alert_location)
        TextView location;
        @BindView(R.id.alerts_alert_time_icon)
        TextView timeIcon;
        @BindView(R.id.alerts_alert_time)
        TextView time;
        @BindView(R.id.alerts_alert_take_care_button)
        Button takeCareButton;

        public AlertViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
