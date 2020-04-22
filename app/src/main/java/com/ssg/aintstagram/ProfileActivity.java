package com.ssg.aintstagram;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;


public class ProfileActivity extends Activity {
    private static final int REQUEST_TAKE_ALBUM = 2;
    private ImageButton btn_add;
    private ImageButton btn_profile;

    private StringBuilder username = new StringBuilder("");
    private StringBuilder text_comment = new StringBuilder("");
    private int post_cnt = 0;
    private int follower_cnt = 0;
    private int following_cnt = 0;
    private boolean is_open = true;
    Bitmap bitmap;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setBtn();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserProfile();
    }

    public void setBtn() {
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck;
                switch (v.getId()) {
                    case R.id.button_to_add:
                        permissionCheck = ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(ProfileActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_TAKE_ALBUM);
                        }
                        break;

                    case R.id.button_to_info:
                        Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                        startActivity(intent);
                }
            }
        };
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
    }

    public void getUserProfile(){
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSuccess(MeV2Response result) {
                String Nickname = result.getNickname();
                int kakaoID = (int) result.getId();

                String base_URL = "http://10.0.2.2:8000/graphql/";
                OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                ApolloClient apolloClient = ApolloClient.builder().serverUrl(base_URL).okHttpClient(okHttpClient).build();

                final UserTypeQuery u = UserTypeQuery.builder().kakaoID(kakaoID).build();

                apolloClient.query(u).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                        post_cnt = response.data().users().get(0).postCount;
                        follower_cnt = response.data().users().get(0).followerCount;
                        following_cnt = response.data().users().get(0).followingCount;
                        is_open = response.data().users().get(0).isOpen;
                        final String profile_img = getString(R.string.media_url) + response.data().users().get(0).profile;

                        Thread mThread = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    URL img_url = new URL(profile_img);
                                    HttpURLConnection conn = (HttpURLConnection) img_url.openConnection();
                                    conn.setDoInput(true);
                                    conn.connect();

                                    InputStream is = conn.getInputStream();
                                    bitmap = BitmapFactory.decodeStream(is);

                                    runOnUiThread(new Runnable(){
                                        public void run(){
                                            ImageView v_profile = (ImageView)findViewById(R.id.user_profile);
                                            v_profile.setImageBitmap(bitmap);
                                        }
                                    });
                                } catch(
                                MalformedURLException e) {
                                    e.printStackTrace();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                        mThread.start();

                        Button btn_name = (Button)findViewById(R.id.button_to_username);
                        btn_name.setTextColor(Color.WHITE);
                        btn_name.setText(response.data().users().get(0).name);

                        Button btn_posts = (Button)findViewById(R.id.user_posts);
                        btn_posts.setText(String.valueOf(post_cnt)+"\n게시물");
                        btn_posts.setTextColor(Color.WHITE);

                        Button btn_follow = (Button)findViewById(R.id.user_followers);
                        btn_follow.setText(String.valueOf(follower_cnt)+"\n팔로워");
                        btn_follow.setTextColor(Color.WHITE);

                        Button btn_following = (Button)findViewById(R.id.user_followings);
                        btn_following.setText(String.valueOf(following_cnt)+"\n팔로잉");
                        btn_following.setTextColor(Color.WHITE);

                        TextView v_comment = (TextView)findViewById(R.id.user_comment);
                        v_comment.setText(response.data().users().get(0).textComment);
                        v_comment.setTextColor(Color.WHITE);

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {

                    }
                });
            }

        });
    }

}
