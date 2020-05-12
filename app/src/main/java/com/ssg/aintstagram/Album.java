package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class Album {
    private Bitmap img;
    private int record;

    public Album(Bitmap img, int record){
        this.img = img;
        this.record = record;
    }

    public Bitmap getImg(){
        return img;
    }

    public int getRecord() {
        return record;
    }
}


