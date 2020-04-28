package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class Album {
    private Bitmap img;

    public Album(Bitmap img){
        this.img = img;
    }

    public Bitmap getImg(){
        return img;
    }
}
