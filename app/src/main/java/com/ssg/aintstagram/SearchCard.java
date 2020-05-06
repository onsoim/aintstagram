package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class SearchCard {
    private Bitmap img;
    private String name;
    private String comment;

    public SearchCard(Bitmap img, String name, String comment) {
        this.img = img;
        this.name = name;
        this.comment = comment;
    }

    public SearchCard(String name, String comment){
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
}
