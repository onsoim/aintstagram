package com.ssg.aintstagram;


import android.graphics.Bitmap;

public class Chatroom {
    private Bitmap img;
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

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public Bitmap getImg() {
        return img;
    }
}
