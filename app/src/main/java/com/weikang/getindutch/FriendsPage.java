package com.weikang.getindutch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FriendsPage extends Fragment {
    private static final String TAG = "FriendsPageFragment";

    private ListView mFriendsListView;
    private FriendsAdapter mFriendsAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends_page,container,false);

        //List<Users> friendsList = new ArrayList<>();
        //mFriendsAdapter = new FriendsAdapter(MainPage.this, R.layout.items_friends, friendsList);
        //mFriendsListView.setAdapter(mFriendsAdapter);

        return view;
    }
}