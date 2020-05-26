package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class HistoryCard {
    private Bitmap img;
    private String type_info;
    private String date;

    public HistoryCard(Bitmap img, String type_info, String date) {
        this.img = img;
        this.type_info = type_info;
        this.date = date;
    }

    public HistoryCard(String type_info, String date){
        this.type_info = type_info;
        this.date = date;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getTypeInfo() {
        return type_info;
    }

    public String getDate() {
        return date;
    }

    public void setImg(Bitmap img){
        this.img = img;
    }
}
