package com.ssg.aintstagram;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;
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


public class SearchResultProfileActivity extends FragmentActivity {
    private static final int REQUEST_TAKE_ALBUM = 2;
    public String name;
    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_home;
    private ImageButton btn_history;
    private ImageButton btn_search;
    private Button btn_edit_profile;
    private Button btn_follow;
    private Button btn_msg;
    private Button btn_edit;

    private FragmentManager fragmentManager;
    private SearchProfileFragment fragmentA;
    private FragmentTransaction transaction;

    private int post_cnt = 0;
    private int follower_cnt = 0;
    private int following_cnt = 0;
    private boolean is_open = true;

    private int myKakaoId;
    private int searchKakaoId;

    String Token;
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

        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                finish();
            } else {
                name = extras.getString("username");
            }
        } else {
            name = (String) savedInstanceState.getSerializable("username");
        }

        if(name == null){
            finish();
        }

        setBtn();

        fragmentManager = getSupportFragmentManager();
        fragmentA = new SearchProfileFragment();
        fragmentA.setName(name);
        transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_A, fragmentA).commitAllowingStateLoss();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserProfile();
    }

    public void setBtn() {
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);
        btn_home = (ImageButton) findViewById(R.id.button_to_home);
        btn_search = (ImageButton) findViewById(R.id.button_to_search);
        btn_edit_profile = (Button) findViewById(R.id.button_edit_profile);
        btn_history = (ImageButton) findViewById(R.id.button_to_history);

        View.OnClickListener Listener = new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                int permissionCheck;
                Intent intent = new Intent();
                switch (v.getId()) {
                    case R.id.button_to_home:
                        intent = new Intent(SearchResultProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;

                    case R.id.button_to_search:
                        Intent searchintent = new Intent(SearchResultProfileActivity.this, SearchActivity.class);
                        searchintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(searchintent);
                        break;

                    case R.id.button_to_add:
                        permissionCheck = ContextCompat.checkSelfPermission(SearchResultProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(SearchResultProfileActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent addintent = new Intent();
                            addintent.setAction(Intent.ACTION_GET_CONTENT);
                            addintent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            addintent.setType("image/*");
                            startActivityForResult(addintent, REQUEST_TAKE_ALBUM);
                        }
                        break;

                    case R.id.button_to_info:
                        Intent infointent = new Intent(SearchResultProfileActivity.this, ProfileActivity.class);
                        infointent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(infointent);
                        break;

                    case R.id.button_edit_profile:
                        intent = new Intent(SearchResultProfileActivity.this, EditProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        break;

                    case R.id.button_to_history:
                        Intent historyIntent = new Intent(SearchResultProfileActivity.this, HistoryActivity.class);
                        historyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(historyIntent);
                        break;
                }
            }
        };
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
        btn_home.setOnClickListener(Listener);
        btn_search.setOnClickListener(Listener);
        btn_edit_profile.setOnClickListener(Listener);
        btn_history.setOnClickListener(Listener);
    }

    public void setRenewedButton(){
        View.OnClickListener mListener = new View.OnClickListener() {
            @SuppressLint("IntentReset")
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.button_follow:
                        if(btn_follow.getText() == "팔로우"){
                            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
                            final Add_followMutation f = Add_followMutation.builder().accessToken(Token).fkakaoID(searchKakaoId).build();
                            apolloClient.mutate(f).enqueue(new ApolloCall.Callback<Add_followMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<Add_followMutation.Data> response) {
                                    getUserProfile();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "요청을 완료하였습니다.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {

                                }
                            });
                        } else {
                            final OkHttpClient okHttpClient2 = new OkHttpClient.Builder().build();
                            final ApolloClient apolloClient2 = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient2).build();
                            final Un_followMutation u = Un_followMutation.builder().accessToken(Token).fkakaoID(searchKakaoId).build();
                            apolloClient2.mutate(u).enqueue(new ApolloCall.Callback<Un_followMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<Un_followMutation.Data> response) {
                                    getUserProfile();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "요청을 완료하였습니다.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {

                                }
                            });
                        }
                        break;
                }
            }
        };
        btn_follow.setOnClickListener(mListener);
    };

    public void getUserProfile(){
        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                Intent intent = new Intent(SearchResultProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSuccess(MeV2Response result) {
                String Nickname = result.getNickname();
                myKakaoId = (int) result.getId();

                final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
                final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();

                final UserTypeQuery u = UserTypeQuery.builder().name(name).build();

                apolloClient.query(u).enqueue(new ApolloCall.Callback<UserTypeQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<UserTypeQuery.Data> response) {
                        post_cnt = response.data().users().get(0).postCount;
                        follower_cnt = response.data().users().get(0).followerCount;
                        following_cnt = response.data().users().get(0).followingCount;
                        is_open = response.data().users().get(0).isOpen;
                        searchKakaoId = response.data().users().get(0).kakaoID;
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

                                            if(myKakaoId != searchKakaoId) {
                                                btn_follow = (Button) findViewById(R.id.button_follow);
                                                btn_follow.setVisibility(View.VISIBLE);
                                                btn_msg = (Button) findViewById(R.id.button_message);
                                                btn_msg.setVisibility(View.VISIBLE);
                                                btn_edit = (Button) findViewById(R.id.button_edit_profile);
                                                btn_edit.setVisibility(View.GONE);
                                                ConstraintSet constraintSet = new ConstraintSet();
                                                ConstraintLayout constraintLayout = findViewById(R.id.profile);
                                                constraintSet.clone(constraintLayout);
                                                constraintSet.connect(R.id.my_pics, ConstraintSet.TOP, R.id.button_follow, ConstraintSet.BOTTOM, 0);
                                                constraintSet.connect(R.id.others_pics, ConstraintSet.TOP, R.id.button_message, ConstraintSet.BOTTOM, 0);
                                                constraintSet.applyTo(constraintLayout);

                                                setRenewedButton();
                                                getFollowInfo();
                                            }

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
                        btn_name.setText(response.data().users().get(0).name);

                        Button btn_posts = (Button)findViewById(R.id.user_posts);
                        btn_posts.setText(String.valueOf(post_cnt)+"\n게시물");

                        Button btn_follow = (Button)findViewById(R.id.user_followers);
                        btn_follow.setText(String.valueOf(follower_cnt)+"\n팔로워");

                        Button btn_following = (Button)findViewById(R.id.user_followings);
                        btn_following.setText(String.valueOf(following_cnt)+"\n팔로잉");

                        TextView v_comment = (TextView)findViewById(R.id.user_comment);
                        v_comment.setText(response.data().users().get(0).textComment);

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {

                    }
                });

            }

        });
    }

    public void getFollowInfo(){
        if(myKakaoId == searchKakaoId) return;

        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final OkHttpClient okHttpClient2 = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient2 = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient2).build();

        final FollowTypeQuery q = FollowTypeQuery.builder().accessToken(Token).username(name).build();

        apolloClient2.query(q).enqueue(new ApolloCall.Callback<FollowTypeQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<FollowTypeQuery.Data> response) {
                if (response.data().follows().size() == 1){
                    btn_follow = (Button) findViewById(R.id.button_follow);
                    btn_follow.setText("팔로잉");
                } else {
                    btn_follow = (Button) findViewById(R.id.button_follow);
                    btn_follow.setText("팔로우");
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    };


}