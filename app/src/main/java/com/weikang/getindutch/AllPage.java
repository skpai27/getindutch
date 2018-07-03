package com.weikang.getindutch;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AllPage extends Fragment {
    private static final String TAG = "AllPageFragment";

    private Dialog mDialogAddpopup;
    private Dialog mDialogCreateGroup;

    private Spinner mSortDropdown;
    private FloatingActionButton mAddButton;
    private RecyclerView mRecyclerView;

    //variables for recyclerview Adapter
    private ArrayList<Groups> mGroups = new ArrayList<>();
    private AllPageAdapter mAdapter;

    //Firebase variables
    //Database
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mGroupsDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mGroupDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_page,container,false);

        mAddButton = (FloatingActionButton) view.findViewById(R.id.addBtn);
        mSortDropdown = (Spinner) view.findViewById(R.id.dropDown);
        mDialogAddpopup = new Dialog(getActivity());
        mDialogCreateGroup = new Dialog(getActivity());

        //Initialise Adapter and recyclerview etc
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        //use getActivity() instead of (this) for context cos this is a fragment
        mAdapter = new AllPageAdapter(mGroups, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //initialise Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mGroupsDatabaseReference = mFirebaseDatabase.getReference().child("users").child(mAuth.getUid()).child("groups");
        //change the names in future to avoid confusion
        mGroupDatabaseReference = mFirebaseDatabase.getReference().child("groups");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPopup(v);
            }
        });

        //database child event listener
        startView();
        return view;
    }

    public void startView(){
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                //datasnapshot contains data at that location when listener is triggered
                //first adding group object into the arraylist, then use adpater.notifyiteminserted

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final String groupName = dataSnapshot.getKey();
                    mAdapter.clear();
                    mFirebaseDatabase.getReference().child("groups").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            for (DataSnapshot datas : dataSnapshot1.getChildren()){
                                if(datas.getKey().equals(groupName)) {
                                    Groups group = datas.getValue(Groups.class);
                                    mGroups.add(group);
                                    mAdapter.notifyItemInserted(mGroups.size() - 1);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            //add a child event listener. SO the reference (mGroupDatabaseRef) defines which part of
            //database to listen to, mchildEventlistener defines what to do
            mGroupsDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    public void switchActivity(){
        mAdapter.clear();
        mChildEventListener = null;
    }

    public void showAddPopup(View view){
        mDialogAddpopup.setContentView(R.layout.add_pop_up);
        TextView textclose = (TextView) mDialogAddpopup.findViewById(R.id.text_close);
        Button manualAdd = (Button) mDialogAddpopup.findViewById(R.id.manual_add);
        Button receiptScan = (Button) mDialogAddpopup.findViewById(R.id.scanner_receipt);
        Button createGroup = (Button) mDialogAddpopup.findViewById(R.id.create_group);
        textclose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogAddpopup.dismiss();
            }
        });
        mDialogAddpopup.show();
        mDialogAddpopup.setCancelable(true);
        manualAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switchActivity();
                Intent intent = new Intent(getActivity(), AddExpenses.class);
                startActivity(intent);
            }
        });
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroup(v);
            }
        });
    }

    public void showCreateGroup(View view){
        TextView cancelBtn;
        TextView nextBtn;
        final EditText groupName;
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mDialogCreateGroup.setContentView(R.layout.create_group_popup);
        cancelBtn = (TextView) mDialogCreateGroup.findViewById(R.id.cancelBtn);
        nextBtn = (TextView) mDialogCreateGroup.findViewById(R.id.nextBtn);
        groupName = (EditText) mDialogCreateGroup.findViewById(R.id.groupName);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogCreateGroup.dismiss();
            }
        });
        mDialogCreateGroup.show();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currGroupName = groupName.getText().toString();
                if (currGroupName.isEmpty()){
                    Toast.makeText(getActivity(),"Please enter a group name!", Toast.LENGTH_SHORT).show();
                } else {
                    final Groups newGroup = new Groups(currGroupName, new HashMap<String, Float>()); //not really sure why it has to be final
                    final DatabaseReference groupRef = mGroupDatabaseReference.child(currGroupName);
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                //create new user
                                mDialogCreateGroup.dismiss();
                                mDialogAddpopup.dismiss();
                                mUsersDatabaseReference.child(user.getUid()).child("groups").child(currGroupName).setValue(true);
                                newGroup.addMembers(user.getUid());
                                groupRef.setValue(newGroup);
                                Toast.makeText(getActivity(),currGroupName + " has been successfully created!", Toast.LENGTH_SHORT).show();
                                mChildEventListener = null;

                            } else {
                                Toast.makeText(getActivity(), "Name of group has been taken. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    groupRef.addListenerForSingleValueEvent(eventListener);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        switchActivity();
    }
}