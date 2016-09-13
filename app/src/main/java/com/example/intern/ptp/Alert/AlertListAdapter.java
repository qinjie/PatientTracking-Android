package com.example.intern.ptp.Alert;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.FontManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertListAdapter extends BaseAdapter {
    private final LayoutInflater inflater;
    private Context context;
    private List<Alert> ongoingAlerts;
    private List<Alert> solvedAlerts;

    private static final int ONGOING_LABEL_ITEM_COUNT = 1;
    private static final int SOLVED_LABEL_ITEM_COUNT = 1;

    private static final int VIEWTYPE_LABEL = 0;
    private static final int VIEWTYPE_ALERT = 1;

    public AlertListAdapter(Context context, List<Alert> alerts) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        ongoingAlerts = new ArrayList<>(alerts.size());
        solvedAlerts = new ArrayList<>(alerts.size());

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
        if (position == 0 || position == ongoingAlerts.size() + ONGOING_LABEL_ITEM_COUNT) {
            return VIEWTYPE_LABEL;
        } else {
            return VIEWTYPE_ALERT;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        Resources res = context.getResources();

        switch (getItemViewType(position)) {
            case VIEWTYPE_LABEL: {
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

                if (position == 0) {
                    holder.icon.setText(res.getString(R.string.fa_icon_bell));

                    String text = String.format(Locale.getDefault(), res.getString(R.string.alerts_alert_ongoing_alerts), ongoingAlerts.size());
                    holder.text.setText(text);
                } else {
                    holder.icon.setText(res.getString(R.string.fa_check_square));

                    String text = String.format(Locale.getDefault(), res.getString(R.string.alerts_alert_solved_alerts), solvedAlerts.size());
                    holder.text.setText(text);
                }

                break;
            }

            case VIEWTYPE_ALERT: {
                AlertViewHolder holder;

                if (view != null && view.getTag() instanceof LabelViewHolder) {
                    holder = (AlertViewHolder) view.getTag();
                } else {
                    view = inflater.inflate(R.layout.item_alerts_alert, parent, false);
                    holder = new AlertViewHolder(view);
                    holder.locationIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                    holder.timeIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
                }

                Alert alert = (Alert) getItem(position);

                String title = String.format(Locale.getDefault(), res.getString(R.string.alert_title), alert.getFirstname(), alert.getLastname());
                holder.title.setText(title);

                holder.location.setText(alert.getLastPositionLabel());

                holder.time.setText(alert.getCreatedAt());

                if (!alert.getOk().equalsIgnoreCase("0")) {
                    holder.takeCareButton.setVisibility(View.INVISIBLE);
                }
            }
        }

        return view;
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
