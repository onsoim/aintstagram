package com.ssg.aintstagram;

import android.graphics.Bitmap;


public class FollowCard {
    private Bitmap img;
    private String name;
    private String comment;
    private Integer kakaoID;
    private Boolean isMutual;

    public FollowCard(Bitmap img, String name, String comment, Integer kakaoID, Boolean isMutual){
        this.img = img;
        this.name = name;
        this.comment = comment;
        this.kakaoID = kakaoID;
        this.isMutual = isMutual;
    }

    public FollowCard(String name, String comment, Integer kakaoID, Boolean isMutual){
        this.name = name;
        this.comment = comment;
        this.kakaoID = kakaoID;
        this.isMutual = isMutual;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public Integer getKakaoID() { return kakaoID; }

    public Boolean getIsMutual(){
        return isMutual;
    }

    public void setIsMutual(Boolean isMutual){
        this.isMutual = isMutual;
    }

}

