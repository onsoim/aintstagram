package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class FollowCard {
    private Bitmap img;
    private String name;
    private String comment;
    private Boolean isMutual;

    public FollowCard(Bitmap img, String name, String comment){
        this.img = img;
        this.name = name;
        this.comment = comment;
        this.isMutual = isMutual;
    }

    public FollowCard(String name, String comment){
        this.name = name;
        this.comment = comment;
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

    public Boolean getIsMutual(){
        return isMutual;
    }

}

