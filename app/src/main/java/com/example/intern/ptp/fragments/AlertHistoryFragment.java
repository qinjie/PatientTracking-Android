package com.example.intern.ptp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.network.models.Alert;
import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.views.adapter.AlertHistoryListAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class AlertHistoryFragment extends BaseFragment {

    @BindView(R.id.alerthistory_list)
    ListView alertHistoryList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_alert_history, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        ArrayList<Alert> alerts = args.getParcelableArrayList(Preferences.BUNDLE_KEY_ALERT);

        ListAdapter adapter = new AlertHistoryListAdapter(getActivity(), alerts);
        alertHistoryList.setAdapter(adapter);
    }

    public void refresh(List<Alert> alerts) {
        AlertHistoryListAdapter adapter = (AlertHistoryListAdapter) alertHistoryList.getAdapter();
        adapter.updateAlerts(alerts);
    }
}
