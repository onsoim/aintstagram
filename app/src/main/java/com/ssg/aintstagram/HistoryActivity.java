package com.ssg.aintstagram;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class HistoryActivity extends Activity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_ALBUM = 2;

    private ImageButton btn_add;
    private ImageButton btn_profile;
    private ImageButton btn_search;
    private ImageButton btn_home;
    private ImageButton btn_history;

    private String Token;

    private ArrayList<HistoryCard> histories_days;
    private ArrayList<HistoryCard> histories_month;
    private ArrayList<HistoryCard> histories_months;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ArrayList<UserPostActivity.ImgUrlThread> threads;
    RecyclerView recycler_days;
    RecyclerView recycler_month;
    RecyclerView recycler_months;
    HistoryRecyclerAdapter adapter_days;
    HistoryRecyclerAdapter adapter_month;
    HistoryRecyclerAdapter adapter_months;

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
        getHistory();

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
        histories_days = new ArrayList<>();
        histories_month = new ArrayList<>();
        histories_months = new ArrayList<>();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final HistoryTypeQuery q = HistoryTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(q).enqueue(new ApolloCall.Callback<HistoryTypeQuery.Data>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NotNull Response<HistoryTypeQuery.Data> response) {
                int cnt = response.data().histories().size();

                for(int i=0; i<cnt; i++){
                    int history_id = Integer.parseInt(response.data().histories().get(i).historyId);
                    int record = response.data().histories().get(i).recordId;
                    String type_info = String.valueOf(response.data().histories().get(i).type);
                    boolean isActive = response.data().histories().get(i).isActive;
                    String dt = response.data().histories().get(i).date.toString();
                    ZonedDateTime zdt = ZonedDateTime.parse(dt);
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    long days = Duration.between(zdt ,now).toDays();
                    long hours = Duration.between(zdt, now).toHours();
                    long mins = Duration.between(zdt, now).toMinutes();

                    if(days>=1) {
                        if(days<=7)
                            histories_days.add(new HistoryCard(type_info, String.valueOf(days) + "일"));
                        else if(days<=30)
                            histories_month.add(new HistoryCard(type_info, String.valueOf(days) + "일"));
                        else
                            histories_months.add(new HistoryCard(type_info, String.valueOf(days) + "일"));
                    } else if(hours>=1){
                        histories_days.add(new HistoryCard(type_info, String.valueOf(hours) + "시간"));
                    } else {
                        histories_days.add(new HistoryCard(type_info, String.valueOf(mins) + "분"));
                    }

                    Log.e("DEBUG", type_info);

                    Thread mThread = new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        HistoryRecyclerAdapter.OnCardListener onCardListener = new HistoryRecyclerAdapter.OnCardListener() {
                                            @Override
                                            public void onCardClick(int pos) {

                                            }
                                        };

                                        adapter_days = new HistoryRecyclerAdapter(histories_days, getApplicationContext(), onCardListener);
                                        recycler_days.setAdapter(adapter_days);

                                        adapter_month = new HistoryRecyclerAdapter(histories_month, getApplicationContext(), onCardListener);
                                        recycler_month.setAdapter(adapter_month);

                                        adapter_months = new HistoryRecyclerAdapter(histories_months, getApplicationContext(), onCardListener);
                                        recycler_months.setAdapter(adapter_months);
                                    }
                                });
                            } catch(Exception e){

                            }
                        }
                    };

                    mThread.start();
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });

    }


}
