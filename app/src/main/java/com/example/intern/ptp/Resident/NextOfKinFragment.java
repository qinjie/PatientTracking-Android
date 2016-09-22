package com.example.intern.ptp.Resident;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.intern.ptp.R;

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

        return view;
    }
}
