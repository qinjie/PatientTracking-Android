package com.example.intern.ptp.Navigation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.intern.ptp.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationListAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<NavigationItem> items;

    public NavigationListAdapter(Context context, List<NavigationItem> items) {
        inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getItemViewType(int position) {
        NavigationItem item = (NavigationItem) getItem(position);
        return item.getType();
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

        switch (getItemViewType(position)) {
            case NavigationItem.VIEWTYPE_PROFILE: {
                ProfileViewHolder holder;
                ProfileNavigationItem item = (ProfileNavigationItem) getItem(position);

                if (view != null && view.getTag() instanceof ProfileViewHolder) {
                    holder = (ProfileViewHolder) view.getTag();

                } else {
                    view = inflater.inflate(R.layout.item_navigation_profile, parent, false);
                    holder = new ProfileViewHolder(view);
                    view.setTag(holder);
                }

                holder.name.setText(item.getName());
                holder.mail.setText(item.getMail());

                break;
            }

            case NavigationItem.VIEWTYPE_DIVIDER: {

                if (view == null) {
                    view = inflater.inflate(R.layout.item_navigation_divider, parent, false);
                }

                break;
            }

            case NavigationItem.VIEWTYPE_PRIMARY: {
                PrimaryViewHolder holder;
                PrimaryNavigationItem item = (PrimaryNavigationItem) getItem(position);

                if (view != null && view.getTag() instanceof PrimaryViewHolder) {
                    holder = (PrimaryViewHolder) view.getTag();
                } else {
                    view = inflater.inflate(R.layout.item_navigation_primary, parent, false);
                    holder = new PrimaryViewHolder(view);
                    view.setTag(holder);
                }

                holder.icon.setText(item.getFontIcon());
                holder.label.setText(item.getLabel());
                holder.additionalInformation.setText(item.getAdditionalInformation());

                break;
            }

            case NavigationItem.VIEWTYPE_SECONDARY: {
                SecondaryViewHolder holder;
                SecondaryNavigationItem item = (SecondaryNavigationItem) getItem(position);

                if(view != null && view.getTag() instanceof  SecondaryViewHolder) {
                    holder = (SecondaryViewHolder) view.getTag();
                } else {
                    view = inflater.inflate(R.layout.item_navigation_secondary, parent, false);
                    holder = new SecondaryViewHolder(view);
                    view.setTag(holder);
                }

                holder.label.setText(item.getLabel());

                break;
            }
        }

        return view;
    }

    static class ProfileViewHolder {
        @BindView(R.id.navigation_profile_name)
        TextView name;
        @BindView(R.id.navigation_profile_mail)
        TextView mail;

        public ProfileViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class PrimaryViewHolder {
        @BindView(R.id.navigation_primary_icon)
        TextView icon;
        @BindView(R.id.navigation_primary_label)
        TextView label;
        @BindView(R.id.navigation_primary_additional_information)
        TextView additionalInformation;

        public PrimaryViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class SecondaryViewHolder {
        @BindView(R.id.navigation_secondary_label)
        TextView label;

        public SecondaryViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
