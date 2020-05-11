package com.ssg.aintstagram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FollowActivity extends FragmentActivity {
    private static final int REQUEST_TAKE_ALBUM = 2;
    private FragmentManager fragmentManager;
    private FollowerFragment followerFragment;
    private FollowingFragment followingFragment;
    private FragmentTransaction transaction;

    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_home;
    private ImageButton btn_search;
    private Button btn_edit_profile;

    private Integer choice = -1;
    private Integer follower_cnt = 0;
    private Integer following_cnt = 0;

    private Button btn_follower;
    private Button btn_following;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followinfo);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
            } else {
                choice = extras.getInt("choice");
                follower_cnt = extras.getInt("follower");
                following_cnt = extras.getInt("following");
            }
        } else {
            choice = (Integer) savedInstanceState.getSerializable("choice");
            follower_cnt = (Integer) savedInstanceState.getSerializable("follower");
            following_cnt = (Integer) savedInstanceState.getSerializable("following");
        }

        setBtn();

        fragmentManager = getSupportFragmentManager();
        followerFragment = new FollowerFragment();
        followingFragment = new FollowingFragment();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.follow_frame, followerFragment);
        transaction.add(R.id.follow_frame, followingFragment);
        setFragment(choice);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void setBtn() {
        btn_follower = (Button) findViewById(R.id.btn_follower);
        btn_following = (Button) findViewById(R.id.btn_following);
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);
        btn_home = (ImageButton) findViewById(R.id.button_to_home);
        btn_edit_profile = (Button) findViewById(R.id.button_edit_profile);
        btn_search = (ImageButton) findViewById(R.id.button_to_search);

        btn_follower.setText(String.valueOf(follower_cnt)+" 팔로워");
        btn_following.setText(String.valueOf(following_cnt)+" 팔로잉");

        View.OnClickListener Listener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int permissionCheck;
                switch(v.getId()){
                    case R.id.button_to_home:
                        Intent homeIntent = new Intent(FollowActivity.this, MainActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(homeIntent);
                        break;

                    case R.id.button_to_search:
                        Intent searchIntent = new Intent(FollowActivity.this, SearchActivity.class);
                        searchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(searchIntent);
                        break;

                    case R.id.button_to_add:
                        permissionCheck = ContextCompat.checkSelfPermission(FollowActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(FollowActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent addIntent = new Intent();
                            addIntent.setAction(Intent.ACTION_GET_CONTENT);
                            addIntent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            addIntent.setType("image/*");
                            startActivityForResult(addIntent, REQUEST_TAKE_ALBUM);
                        }
                        break;

                    case R.id.button_to_info:
                        Intent infoIntent = new Intent(FollowActivity.this, ProfileActivity.class);
                        infoIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(infoIntent);
                        break;

                    case R.id.btn_follower:
                        choice = 1;
                        setFragment(choice);
                        break;

                    case R.id.btn_following:
                        choice = 2;
                        setFragment(choice);
                        break;
                }
            }
        };

        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
        btn_home.setOnClickListener(Listener);
        btn_search.setOnClickListener(Listener);
        btn_follower.setOnClickListener(Listener);
        btn_following.setOnClickListener(Listener);
    }

    public void setFragment(int c){
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        switch(c){
            case 1:
                if(followerFragment!=null) transaction.replace(R.id.follow_frame, followerFragment).commit();
                break;
            case 2:
                if(followingFragment!=null) transaction.replace(R.id.follow_frame, followingFragment).commit();
                break;
            default:
                finish();
                break;
        }
    }

}