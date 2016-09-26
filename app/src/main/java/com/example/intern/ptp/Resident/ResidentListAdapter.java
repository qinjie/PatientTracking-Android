package com.example.intern.ptp.Resident;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.intern.ptp.R;
import com.example.intern.ptp.utils.FontManager;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResidentListAdapter extends BaseAdapter {

    // TODO: REMOVE THIS. JUST SEPT2016 DEMO
    private static final int MAX_RESIDENTS = 4;
    private static int residentNr = 0;
    private HashMap<String, String> residentProfilePics;
    // TODO END

    private LayoutInflater inflater;
    private Context context;
    private List<Resident> items;

    public ResidentListAdapter(Context context, List<Resident> residents) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = residents;

        // TODO: REMOVE THIS. JUST SEPT2016 DEMO
        residentProfilePics = new HashMap<>();
        // TODO END
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
            view = inflater.inflate(R.layout.item_resident, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
            holder.locationIcon.setTypeface(FontManager.getTypeface(context, FontManager.FONTAWESOME));
        }

        Resident resident = items.get(position);

        holder.name.setText(resident.getFirstname() + " " + resident.getLastname());
        holder.location.setText(resident.getLabel());

        //TODO: REMOVE THIS. IS JUST FOR 2016 SEPT DEMO
        String profilePicture = residentProfilePics.get(resident.getId());

        if (profilePicture == null) {
            profilePicture = "resident" + (residentNr % MAX_RESIDENTS);
            residentProfilePics.put(resident.getId(), profilePicture);
            residentNr++;
        }

        Drawable image = context.getDrawable(context.getResources().getIdentifier(profilePicture, "drawable", context.getPackageName()));
        holder.profilePicture.setImageDrawable(image);
        // TODO END

        return view;
    }

    public void setResidents(List<Resident> residents) {
        this.items = residents;
        notifyDataSetChanged();

    }

    static class ViewHolder {
        @BindView(R.id.resident_profile_picture)
        ImageView profilePicture;
        @BindView(R.id.resident_name)
        TextView name;
        @BindView(R.id.resident_location_icon)
        TextView locationIcon;
        @BindView(R.id.resident_location)
        TextView location;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
