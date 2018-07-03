package com.weikang.getindutch;

import android.app.Dialog;
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
import android.widget.EditText;
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

public class FriendsPage extends Fragment {
    private static final String TAG = "FriendsPageFragment";

    private FloatingActionButton mAddButton;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;

    //variables for recyclerview Adapter
    private ArrayList<Users> mFriends = new ArrayList<>();
    private FriendsAdapter mAdapter;

    //Firebase variables
    //Database
    private FirebaseUser user;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendsDatabaseReference;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mUsersDatabaseReference;

    //Dialog
    private Dialog mDialogAddFriend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends_page,container,false);

        mAddButton = (FloatingActionButton) view.findViewById(R.id.friendsAddPopup);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //Initialise Adapter and recyclerview etc
        mRecyclerView = (RecyclerView) view.findViewById(R.id.friendsRecycler);
        //use getActivity() instead of (this) for context cos this is a fragment
        mAdapter = new FriendsAdapter(mFriends, getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //initialise Firebase variables
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFriendsDatabaseReference = mFirebaseDatabase.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("friends");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("users");

        mDialogAddFriend = new Dialog(getActivity());

        //database child event listener
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                //datasnapshot contains data at that location when listener is triggered
                //first adding group object into the arraylist, then use adpater.notifyiteminserted

                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final String friendUid = dataSnapshot.getKey();
                    mFirebaseDatabase.getReference().child("users").child(friendUid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            Users friend = new Users(friendUid,dataSnapshot1.child("name").getValue().toString());
                            mFriends.add(friend);
                            mAdapter.notifyItemInserted(mFriends.size()-1);
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
            mFriendsDatabaseReference.addChildEventListener(mChildEventListener);
        }

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFriendsAddPopup(v);
            }
        });

        return view;
    }

    public void showFriendsAddPopup(View view){
        TextView cancelBtn;
        TextView nextBtn;
        final EditText userName;
        mDialogAddFriend.setContentView(R.layout.add_friends_popup);
        cancelBtn = (TextView) mDialogAddFriend.findViewById(R.id.cancelBtn2);
        nextBtn = (TextView) mDialogAddFriend.findViewById(R.id.nextBtn2);
        userName = (EditText) mDialogAddFriend.findViewById(R.id.userName);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mDialogAddFriend.dismiss();
            }
        });
        mDialogAddFriend.show();
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String currUserName = userName.getText().toString();
                if (currUserName.isEmpty()){
                    Toast.makeText(getActivity(),"Please enter a username!", Toast.LENGTH_SHORT).show();
                } else {
                    ValueEventListener eventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.exists()) {
                                //create new user
                                Toast.makeText(getActivity(),currUserName + " is not found! Please try again.", Toast.LENGTH_SHORT).show();
                            } else {
                                // THE CODE BELOW IS UNDER THE ASSUMPTION THAT THERE'S ONLY ONE USER WITH THIS USERNAME.
                                for (DataSnapshot friend : dataSnapshot.getChildren()){
                                    final String friendUID = friend.getKey();
                                    final DatabaseReference userRef = mUsersDatabaseReference.child(user.getUid()).child("friends").child(friendUID);
                                    ValueEventListener eventListener2 = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.exists()) {
                                                //create new user
                                                Toast.makeText(getActivity(), currUserName + " has been added.", Toast.LENGTH_SHORT).show();
                                                mUsersDatabaseReference.child(user.getUid()).child("friends").child(friendUID).setValue(true);
                                                mUsersDatabaseReference.child(friendUID).child("friends").child(user.getUid()).setValue(true);
                                                mDialogAddFriend.dismiss();
                                                mChildEventListener = null;
                                            } else {
                                                Toast.makeText(getActivity(),currUserName + " is already your friend!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {}
                                    };
                                    userRef.addListenerForSingleValueEvent(eventListener2);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    mUsersDatabaseReference.orderByChild("name").equalTo(currUserName).addListenerForSingleValueEvent(eventListener);
                }
            }
        });
    }
}