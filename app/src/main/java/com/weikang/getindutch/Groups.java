package com.weikang.getindutch;

import com.google.firebase.database.DatabaseReference;

public class Groups {
    private String name;

    public Groups(String name){
        name = name;
    }

    public String getName() {
        return name;
    }

    public void addMembers(Users friend, DatabaseReference mFriendsDatabaseRef){
        mFriendsDatabaseRef.child(friend.getName()).setValue(true);
    }
}
