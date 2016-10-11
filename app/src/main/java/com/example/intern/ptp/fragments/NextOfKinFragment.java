package com.example.intern.ptp.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.intern.ptp.utils.Preferences;
import com.example.intern.ptp.R;
import com.example.intern.ptp.views.adapter.NextOfKinListAdapter;
import com.example.intern.ptp.network.models.NextOfKin;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NextOfKinFragment extends Fragment {

    @BindView(R.id.nextofkin_list)
    ListView nextOfKinList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_next_of_kin, null);
        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        ArrayList<NextOfKin> nextOfKins = arguments.getParcelableArrayList(Preferences.BUNDLE_KEY_NEXT_OF_KINS);

        NextOfKinListAdapter adapter = new NextOfKinListAdapter(getActivity(), nextOfKins);
        nextOfKinList.setAdapter(adapter);

        return view;
    }

    public void refresh(List<NextOfKin> nextOfKins) {
        NextOfKinListAdapter adapter = (NextOfKinListAdapter) nextOfKinList.getAdapter();
        adapter.setNextOfKins(nextOfKins);
    }
}
