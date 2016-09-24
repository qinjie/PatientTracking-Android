package com.example.intern.ptp.Alert;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.intern.ptp.Preferences;
import com.example.intern.ptp.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertHistoryFragment extends Fragment {

    @BindView(R.id.alerthistory_list)
    ListView alertHistoryList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_alert_history, null);
        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        ArrayList<Alert> alerts = args.getParcelableArrayList(Preferences.BUNDLE_KEY_ALERT);

        ListAdapter adapter = new AlertHistoryAdapter(getActivity(), alerts);
        alertHistoryList.setAdapter(adapter);

        return view;
    }

    public void refresh(List<Alert> alerts) {
        AlertHistoryAdapter adapter = (AlertHistoryAdapter) alertHistoryList.getAdapter();
        adapter.updateAlerts(alerts);
    }
}
