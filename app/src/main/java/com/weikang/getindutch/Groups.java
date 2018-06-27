package com.weikang.getindutch;

import java.util.ArrayList;
import java.util.List;

public class Groups {
    private String _groupName;
    private List<Friends> _groupMembers;

    public Groups(String name){
        _groupName = name;
        _groupMembers = new ArrayList<>();
    }

    public String get_groupName() {
        return _groupName;
    }

    public void addMembers(Friends friend){
        _groupMembers.add(friend);
    }
}
