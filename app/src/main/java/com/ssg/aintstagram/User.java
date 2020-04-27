package com.ssg.aintstagram;

import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.FileUpload;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;


import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.String;

import okhttp3.OkHttpClient;

public class User {
    public String name;
    public String Token;
    public int post_cnt;
    public int follower_cnt;
    public int following_cnt;
    public boolean is_open;
    public String profile_img;
    public String comment;
    public boolean status;

    public User() {
        this.post_cnt = 0;
        this.follower_cnt = 0;
        this.following_cnt = 0;
        this.is_open = true;
        this.status = false;
    }

    public User(int post_cnt, int follower_cnt, int following_cnt, boolean is_open, String profile_img, String name, String comment) {
        this.post_cnt = post_cnt;
        this.follower_cnt = follower_cnt;
        this.following_cnt = following_cnt;
        this.is_open = is_open;
        this.profile_img = profile_img;
        this.name = name;
        this.comment = comment;
        this.status = true;
    }

    public User uploadProfile(final User query, String api_url, String imageFilePath) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        ApolloClient apolloClient = ApolloClient.builder().serverUrl(api_url).okHttpClient(okHttpClient).build();

        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();
        final Upload_profileMutation uploadProfile = Upload_profileMutation.builder().img(new FileUpload("image/jpg", new File(imageFilePath))).accessToken(this.Token).build();
        apolloClient.mutate(uploadProfile).enqueue(new ApolloCall.Callback<Upload_profileMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<Upload_profileMutation.Data> response) {
                query.status = true;
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                e.printStackTrace();
                query.status = false;
            }
        });
        return query;
    }
}
