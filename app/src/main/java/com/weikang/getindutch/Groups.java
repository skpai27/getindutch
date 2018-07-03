package com.weikang.getindutch;

import java.util.HashMap;

public class Groups {

    private String name;
    private String photoUrl;
    private HashMap<String, Float> members;

    public Groups(String name, String photoUrl, HashMap<String, Float> members){
        this.name = name;
        this.photoUrl = photoUrl;
        this.members = members;
    }

    public Groups(String name, HashMap<String, Float> members){
        this(name, "https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png", members);
    }

    public Groups(){
        this("testGroup", "https://cdn.pixabay.com/photo/2016/11/14/17/39/group-1824145_960_720.png", new HashMap<String, Float>());
    }

    public String getName(){return name;}

    public String getPhotoUrl(){return photoUrl;}

    public HashMap<String, Float> getMembers(){return members;}

    public void setName(String name){this.name = name;}

    public void setPhotoUrl(String photoUrl){this.photoUrl = photoUrl;}

    public void addMembers(String memberUid){
        Float balance = new Float(0);
        members.put(memberUid,balance);
    }

    public String toString(){
      return "Group name: " + name + ", members: " + members.toString();
    }
}
