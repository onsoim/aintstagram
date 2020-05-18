package com.ssg.aintstagram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Date;

public class Post {
    private Bitmap profile_img;
    private Bitmap post_img;
    private Bitmap comment_img;
    private String name;
    private String place;
    private String text_comment;
    private Integer post_id;
    private int likes;
    private Boolean allow_comment;
    private String date;
    private Boolean like_status;
    private Boolean mine;

    public Post(Bitmap profile_img, Bitmap post_img, String name, String place, Integer post_id, int likes, Boolean allow_comment, String text_comment, String date){
        this.profile_img = profile_img;
        this.post_img = post_img;
        this.name = name;
        this.place = place;
        this.post_id = post_id;
        this.likes = likes;
        this.allow_comment = allow_comment;
        this.text_comment = text_comment;
        this.date = date;
        this.like_status = false;
        this.mine = false;
    }

    public Post(Bitmap post_img, String name, String place, Integer post_id, int likes, Boolean allow_comment, String text_comment, String date){
        this.profile_img = null;
        this.post_img = post_img;
        this.name = name;
        this.place = place;
        this.post_id = post_id;
        this.likes = likes;
        this.allow_comment = allow_comment;
        this.text_comment = text_comment;
        this.date = date;
        this.like_status = true;
        this.mine = false;
    }

    public Post(String name, String place, Integer post_id, String text_comment, int likes, String date){
        this.profile_img = null;
        this.post_img = null;
        this.name = name;
        this.place = place;
        this.post_id = post_id;
        this.likes = 0;
        this.allow_comment = null;
        this.text_comment = text_comment;
        this.date = date;
        this.likes = likes;
        this.like_status = false;
        this.mine = false;
    }

    public void set_post_img(Bitmap img){
        this.post_img = img;
    }

    public void set_comment_img(Bitmap img){
        this.comment_img = img;
    }

    public Boolean get_like_status(){
        return like_status;
    }

    public void set_like_status(Boolean like_status){
        this.like_status = like_status;
    }

    public void set_profile_img(Bitmap img){
        this.profile_img = img;
    }

    public Bitmap get_profile_img(){
        return profile_img;
    }

    public Bitmap get_post_img(){
        return post_img;
    }

    public Bitmap get_comment_img(){
        return comment_img;
    }

    public String getName(){
        return name;
    }

    public String get_text_comment(){
        return text_comment;
    }

    public String getPlace(){
        return place;
    }

    public Integer get_post_id(){
        return post_id;
    }

    public void setLikes(int likes){ this.likes = likes; }

    public Integer getLikes(){
        return likes;
    }

    public Boolean get_allow_comment(){
        return get_allow_comment();
    }

    public String getDate(){
        return date;
    }

    public boolean getMine() { return mine; }

    public void setMine(Boolean mine){
        this.mine = mine;
    }
}
