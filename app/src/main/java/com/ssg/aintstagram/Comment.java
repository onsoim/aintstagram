package com.ssg.aintstagram;

import android.graphics.Bitmap;

public class Comment {
    private Bitmap profile_img;
    private String url;
    private String name;
    private String text_comment;
    private String date;
    private Boolean like_status;
    private int likes;
    private int post_id;
    private int record;
    private Boolean mine;
    private Boolean has_to_block;
    private Integer parent;

    public Comment(int post_id, int record, Bitmap profile_img, String name, String text_comment, String date, Integer parent, String url, int likes){
        this.post_id = post_id;
        this.record = record;
        this.profile_img = profile_img;
        this.name = name;
        this.text_comment = text_comment;
        this.date = date;
        this.like_status = false;
        this.mine = false;
        this.likes = likes;
        this.parent = parent;
        if(this.parent == null){
            this.has_to_block = false;
        } else {
            this.has_to_block = true;
        }
        this.url = url;
    }

    public Comment(int post_id, int record, String name, String text_comment, String date, Integer parent, String url, int likes){
        this.post_id = post_id;
        this.record = record;
        this.profile_img = null;
        this.name = name;
        this.text_comment = text_comment;
        this.date = date;
        this.like_status = false;
        this.mine = false;
        this.likes = likes;
        this.parent = parent;
        this.url = url;
        if(this.parent == null){
            this.has_to_block = false;
        } else {
            this.has_to_block = true;
        }
    }

    public int getRecord(){
        return this.record;
    }

    public String getUrl(){
        return this.url;
    }

    public int get_post_id(){
        return this.post_id;
    }

    public Bitmap get_profile_img(){
        return this.profile_img;
    }

    public String getName(){
        return this.name;
    }

    public String getComment(){
        return this.text_comment;
    }

    public Boolean get_like_status(){
        return this.like_status;
    }

    public Boolean isMine(){
        return this.mine;
    }

    public String getDate(){
        return this.date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public void set_profile_img(Bitmap img){
        this.profile_img = img;
    }

    public void set_like_status(Boolean status){
        this.like_status = status;
    }

    public void set_mine(Boolean isMine){
        this.mine = isMine;
    }

    public void setName(String name){
        this.name = name;
    }

    public void set_comment(String comment){
        this.text_comment = comment;
    }

    public int getLikes(){
        return this.likes;
    }

    public void setLikes(int likes){
        this.likes = likes;
    }

    public Boolean needBlock(){
        return this.has_to_block;
    }

    public void setBlock(Boolean status){
        this.has_to_block = status;
    }

    public Integer getParent(){
        return this.parent;
    }
}
