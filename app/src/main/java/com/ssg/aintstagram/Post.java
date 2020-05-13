package com.ssg.aintstagram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.Date;

public class Post {
    private Bitmap profile_img;
    private Bitmap post_img;
    private String name;
    private String place;
    private String text_comment;
    private Integer post_id;
    private Integer likes;
    private Boolean allow_comment;
    private Date date;

    public Post(Bitmap profile_img, Bitmap post_img, String name, String place, Integer post_id, Integer likes, Boolean allow_comment, String text_comment, Date date){
        this.profile_img = profile_img;
        this.post_img = post_img;
        this.name = name;
        this.place = place;
        this.post_id = post_id;
        this.likes = likes;
        this.allow_comment = allow_comment;
        this.text_comment = text_comment;
        this.date = date;
    }

    public Post(Bitmap post_img, String name, String place, Integer post_id, Integer likes, Boolean allow_comment, String text_comment, Date date){
        this.profile_img = null;
        this.post_img = post_img;
        this.name = name;
        this.place = place;
        this.post_id = post_id;
        this.likes = likes;
        this.allow_comment = allow_comment;
        this.text_comment = text_comment;
        this.date = date;
    }

    public Post(String name, String place, Integer post_id, String text_comment){
        this.profile_img = null;
        this.post_img = null;
        this.name = name;
        this.place = place;
        this.post_id = post_id;
        this.likes = 0;
        this.allow_comment = null;
        this.text_comment = text_comment;
        this.date = null;
    }

    public void set_post_img(Bitmap img){
        this.post_img = img;
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

    public Integer getLikes(){
        return likes;
    }

    public Boolean get_allow_comment(){
        return get_allow_comment();
    }

    public Date getDate(){
        return date;
    }

}
