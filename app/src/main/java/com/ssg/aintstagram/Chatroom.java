package com.ssg.aintstagram;


public class Chatroom {
    private int chatroomId;
    private String name;
    private int kakaoID;
    private String profile;

    public Chatroom(int chatroomId, String name, int kakaoID, String profile){
        this.chatroomId = chatroomId;
        this.name = name;
        this.kakaoID = kakaoID;
        this.profile = profile;
    }

    public int getChatroomId() {
        return chatroomId;
    }

    public String getName() { return name; }

    public String getProfile() { return profile; }
}
