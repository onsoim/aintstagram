package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class HistoryCard {
    private int record;
    private Bitmap img;
    private String type_info;
    private String date;
    private String text;
    private String username;
    private Boolean btn;

    public HistoryCard(int record, Bitmap img, String type_info, String date) {
        this.img = img;
        this.type_info = type_info;
        this.date = date;
        this.username = "";
        this.btn = false;
        this.record = record;
        setText();
    }

    public HistoryCard(int record, String type_info, String date){
        this.type_info = type_info;
        this.date = date;
        this.username = "";
        this.btn = false;
        this.record = record;
        setText();
    }

    public int getRecord() {
        return record;
    }

    public void setBtn(Boolean btn) {
        this.btn = btn;
    }

    public Boolean getBtn() {
        return btn;
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

    public String getText() { return text; }

    public String getUsername() { return username; }

    public void setImg(Bitmap img){
        this.img = img;
    }

    public void setUsername(String user){
        this.username = user;
    }

    private void setText(){
        switch(this.type_info){
            case "F":
                this.text = "님이 회원님을 팔로우하기 시작했습니다.";
                break;
            case "L":
                this.text = "님이 회원님의 게시글을 좋아합니다.";
                break;
            case "C":
                this.text = "님이 댓글을 남겼습니다.";
                break;
            default:
                break;
        }
    }
}
