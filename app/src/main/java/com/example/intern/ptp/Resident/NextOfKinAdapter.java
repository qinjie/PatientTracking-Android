package com.example.intern.ptp.Resident;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.FontManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NextOfKinAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<NextOfKin> items;

    public NextOfKinAdapter(Context context, List<NextOfKin> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
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
            view = inflater.inflate(R.layout.item_next_of_kin, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);

            holder.phoneIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
            holder.emailIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
        }

        NextOfKin nextOfKin = items.get(position);

        holder.name.setText(nextOfKin.getFirstName() + " " + nextOfKin.getLastName());
        holder.email.setText(nextOfKin.getEmail());
        holder.phone.setText(nextOfKin.getContact());
        holder.remark.setText(nextOfKin.getRemark());

        return view;
    }

    static class ViewHolder {
        @BindView(R.id.nextofkin_name)
        TextView name;
        @BindView(R.id.nextofkin_phone_icon)
        TextView phoneIcon;
        @BindView(R.id.nextofkin_phone)
        TextView phone;
        @BindView(R.id.nextofkin_email_icon)
        TextView emailIcon;
        @BindView(R.id.nextofkin_email)
        TextView email;
        @BindView(R.id.nextofkin_remark)
        TextView remark;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

