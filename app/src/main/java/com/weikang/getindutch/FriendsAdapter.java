package com.weikang.getindutch;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FriendsAdapter extends ArrayAdapter<Users> {
    public FriendsAdapter(Context context, int resource, List<Users> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.items_friends, parent, false);
        }

        TextView userName = (TextView) convertView.findViewById(R.id.friendsName);
        ImageView pic = (ImageView) convertView.findViewById(R.id.profilePic);

        Users friend = getItem(position);

        //String friendName =
        //userName.setText(friendName);

        return convertView;
    }

}
