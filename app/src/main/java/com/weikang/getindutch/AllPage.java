package com.weikang.getindutch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;

public class AllPage extends Fragment {
    private static final String TAG = "AllPageFragment";

    private Spinner mSortDropdown;
    private FloatingActionButton mAddButton;
    private ListView mItemListView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_page,container,false);

        mAddButton = (FloatingActionButton) view.findViewById(R.id.addBtn);
        mSortDropdown = (Spinner) view.findViewById(R.id.dropDown);
        mItemListView = (ListView) view.findViewById(R.id.itemListView);

        return view;
    }
}