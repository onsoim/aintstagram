package com.ssg.aintstagram;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FollowActivity extends FragmentActivity {
    private FragmentManager fragmentManager;
    private FollowerFragment followerFragment;
    private FollowingFragment followingFragment;
    private FragmentTransaction transaction;

    private Integer choice = -1;
    private Integer follower_cnt = 0;

    private Button btn_follower;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followinfo);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                finish();
            } else {
                choice = extras.getInt("choice");
            }
        } else {
            choice = (Integer) savedInstanceState.getSerializable("choice");
        }

        if (choice == 1) {
            followerFragment = new FollowerFragment();
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.follow_frame, followerFragment).commitAllowingStateLoss();
        } else if (choice == 2) {
            followingFragment = new FollowingFragment();
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.follow_frame, followingFragment).commitAllowingStateLoss();
        } else {
            finish();
        }

        setBtn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        followerFragment.getFollowerList();
    }

    public void setBtn() {
        btn_follower = (Button) findViewById(R.id.btn_follower);
    }


}