package com.ssg.aintstagram;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HistoryActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_ALBUM = 2;

    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_search;
    private ImageButton btn_home;
    private ImageButton btn_history;

    private String Token;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ArrayList<UserPostActivity.ImgUrlThread> threads;
    RecyclerView recycler_days;
    RecyclerView recycler_month;
    RecyclerView recycler_months;
    PostRecyclerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        recycler_days = (RecyclerView) findViewById(R.id.historyOfRecentDays);
        recycler_month = (RecyclerView) findViewById(R.id.historyOfthisMonth);
        recycler_months = (RecyclerView) findViewById(R.id.historyOfMonths);

        this.setBtn();

        LinearLayoutManager days_linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);;
        recycler_days.setLayoutManager(days_linearLayoutManager);

        LinearLayoutManager month_linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);;
        recycler_month.setLayoutManager(month_linearLayoutManager);

        LinearLayoutManager months_linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);;
        recycler_months.setLayoutManager(months_linearLayoutManager);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    public void setBtn() {
        btn_add = (ImageButton) findViewById(R.id.button_to_add);
        btn_profile = (ImageButton) findViewById(R.id.button_to_info);
        btn_home = (ImageButton) findViewById(R.id.button_to_home);
        btn_search = (ImageButton) findViewById(R.id.button_to_search);
        btn_history = (ImageButton) findViewById(R.id.button_to_history);

        View.OnClickListener Listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permissionCheck;
                switch (v.getId()) {
                    case R.id.button_to_home:
                        Intent homeIntent = new Intent(HistoryActivity.this, MainActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(homeIntent);
                        break;

                    case R.id.button_to_search:
                        Intent searchIntent = new Intent(HistoryActivity.this, SearchActivity.class);
                        searchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(searchIntent);
                        break;

                    case R.id.button_to_add:
                        permissionCheck = ContextCompat.checkSelfPermission(HistoryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(HistoryActivity.this, PERMISSIONS, 0);
                        } else {
                            Intent addIntent = new Intent();
                            addIntent.setAction(Intent.ACTION_GET_CONTENT);
                            addIntent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            addIntent.setType("image/*");
                            startActivityForResult(addIntent, REQUEST_TAKE_ALBUM);
                        }
                        break;

                    case R.id.button_to_info:
                        Intent infoIntent = new Intent(HistoryActivity.this, ProfileActivity.class);
                        infoIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(infoIntent);
                        break;

                    case R.id.button_to_history:
                        Intent historyIntent = new Intent(HistoryActivity.this, HistoryActivity.class);
                        historyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(historyIntent);

                }
            }
        };
        btn_add.setOnClickListener(Listener);
        btn_profile.setOnClickListener(Listener);
        btn_home.setOnClickListener(Listener);
        btn_search.setOnClickListener(Listener);
        btn_history.setOnClickListener(Listener);
    }

    void getHistory(){
        

    }


}
