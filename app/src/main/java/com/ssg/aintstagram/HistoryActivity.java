package com.ssg.aintstagram;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
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
import com.bumptech.glide.Glide;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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
    private Boolean isLeft;

    private ArrayList<HistoryCard> histories_days;
    private ArrayList<HistoryCard> histories_month;
    private ArrayList<HistoryCard> histories_months;

    private GestureDetector gestureDetector;

    String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private ArrayList<HistoryQueryThread> threads;

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
        threads = new ArrayList<>();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final HistoryTypeQuery q = HistoryTypeQuery.builder().accessToken(Token).build();

        apolloClient.query(q).enqueue(new ApolloCall.Callback<HistoryTypeQuery.Data>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NotNull Response<HistoryTypeQuery.Data> response) {
                int cnt = response.data().histories().size();
                int cur = 0;

                for(int i=0; i<cnt; i++) {
                    int history_id = Integer.parseInt(response.data().histories().get(i).historyId);
                    int record = response.data().histories().get(i).recordId;
                    String type_info = String.valueOf(response.data().histories().get(i).type);
                    boolean isActive = response.data().histories().get(i).isActive;
                    String dt = response.data().histories().get(i).date.toString();
                    ZonedDateTime zdt = ZonedDateTime.parse(dt);
                    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
                    long days = Duration.between(zdt, now).toDays();
                    long hours = Duration.between(zdt, now).toHours();
                    long mins = Duration.between(zdt, now).toMinutes();

                    if (!isActive) continue;

                    if (days >= 1) {
                        if (days <= 7) {
                            histories_days.add(new HistoryCard(history_id, type_info, String.valueOf(days) + "일"));
                            threads.add(new HistoryQueryThread(1, cur, history_id));
                        } else if (days <= 30) {
                            histories_month.add(new HistoryCard(history_id, type_info, String.valueOf(days) + "일"));
                            threads.add(new HistoryQueryThread(2, cur, history_id));
                        } else {
                            histories_months.add(new HistoryCard(history_id, type_info, String.valueOf(days) + "일"));
                            threads.add(new HistoryQueryThread(3, cur, history_id));
                        }
                    } else if (hours >= 1) {
                        histories_days.add(new HistoryCard(history_id, type_info, String.valueOf(hours) + "시간"));
                        threads.add(new HistoryQueryThread(1, cur, history_id));
                    } else {
                        histories_days.add(new HistoryCard(history_id, type_info, String.valueOf(mins) + "분"));
                        threads.add(new HistoryQueryThread(1, cur, history_id));
                    }

                    cur++;
                }


                Thread mThread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    HistoryRecyclerAdapter.OnRemoveListener onRemoveListener_days = new HistoryRecyclerAdapter.OnRemoveListener() {
                                        @Override
                                        public void onRemoveClick(int pos) {
                                            int record = histories_days.get(pos).getRecord();
                                            removeHistory(record);
                                        }
                                    };

                                    adapter_days = new HistoryRecyclerAdapter(histories_days, getApplicationContext(), onRemoveListener_days);
                                    recycler_days.setAdapter(adapter_days);

                                    HistoryRecyclerAdapter.OnRemoveListener onRemoveListener_month = new HistoryRecyclerAdapter.OnRemoveListener() {
                                        @Override
                                        public void onRemoveClick(int pos) {
                                            int record = histories_month.get(pos).getRecord();
                                            removeHistory(record);
                                        }
                                    };

                                    adapter_month = new HistoryRecyclerAdapter(histories_month, getApplicationContext(), onRemoveListener_month);
                                    recycler_month.setAdapter(adapter_month);

                                    HistoryRecyclerAdapter.OnRemoveListener onRemoveListener_months = new HistoryRecyclerAdapter.OnRemoveListener() {
                                        @Override
                                        public void onRemoveClick(int pos) {
                                            int record = histories_months.get(pos).getRecord();
                                            removeHistory(record);
                                        }
                                    };

                                    adapter_months = new HistoryRecyclerAdapter(histories_months, getApplicationContext(), onRemoveListener_months);
                                    recycler_months.setAdapter(adapter_months);

                                    gestureDetector = new GestureDetector(HistoryActivity.this, new GestureDetector.SimpleOnGestureListener(){
                                        private static final int SWIPE_THRESHOLD = 100;
                                        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

                                        @Override
                                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                                            boolean result = false;

                                            float diffY = e2.getY() - e1.getY();
                                            float diffX = e2.getX() - e1.getX();
                                            if (Math.abs(diffX) > Math.abs(diffY)) {
                                                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                                                    if (diffX > 0) {
                                                        onSwipeRight();
                                                    } else {
                                                        onSwipeLeft();
                                                    }
                                                }
                                                result = true;
                                            }
                                            return result;
                                        }

                                        public void onSwipeRight() {
                                            isLeft = true;
                                        }

                                        public void onSwipeLeft() {
                                            isLeft = false;
                                        }

                                    });

                                    recycler_days.addOnItemTouchListener(new CustomOnItemTouchListener(1));
                                    recycler_month.addOnItemTouchListener(new CustomOnItemTouchListener(2));
                                    recycler_months.addOnItemTouchListener(new CustomOnItemTouchListener(3));
                                }
                            });
                        } catch(Exception e){
                        }

                        for(HistoryQueryThread thread : threads){
                            thread.run();
                        }
                    }
                };

                mThread.start();
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    public void removeHistory(int record){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final Deactivate_historyMutation q = Deactivate_historyMutation.builder().accessToken(Token).record(record).build();
        apolloClient.mutate(q).enqueue(new ApolloCall.Callback<Deactivate_historyMutation.Data>() {
            @Override
            public void onResponse(@NotNull Response<Deactivate_historyMutation.Data> response) {
                if(response.data().deactivateHistory().success){
                    getHistory();
                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    private class HistoryQueryThread implements Runnable {
        private String profile_url;
        private String username;
        private int idx;
        private int affinity;
        private int record;

        public HistoryQueryThread(int affinity, int idx, int record){
            this.affinity = affinity;
            this.idx = idx;
            this.record = record;
        }

        @Override
        public void run() {
            getUserInfo();
        }

        private void getUserInfo(){
            final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
            Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

            final Get_history_detailMutation g = Get_history_detailMutation.builder().accessToken(Token).record(record).build();
            apolloClient.mutate(g).enqueue(new ApolloCall.Callback<Get_history_detailMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<Get_history_detailMutation.Data> response) {
                    if(response.data().getHistoryDetail().success){
                        profile_url = getString(R.string.media_url) + response.data().getHistoryDetail().profile;
                        username = response.data().getHistoryDetail().username;

                        try {
                            addProfileImage();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }

                @Override
                public void onFailure(@NotNull ApolloException e) {

                }
            });

        }

        private void addProfileImage() throws ExecutionException, InterruptedException {
            Bitmap bitmap = Glide
                    .with(getApplicationContext())
                    .asBitmap()
                    .load(profile_url)
                    .submit().get();

            switch(affinity){
                case 1:
                    histories_days.get(idx).setImg(bitmap);
                    histories_days.get(idx).setUsername(username);
                    break;
                case 2:
                    histories_month.get(idx).setImg(bitmap);
                    histories_month.get(idx).setUsername(username);
                    break;
                case 3:
                    histories_months.get(idx).setImg(bitmap);
                    histories_months.get(idx).setUsername(username);
                    break;
            }

            NotifyRunnable runnable = new NotifyRunnable(idx, affinity);
            runOnUiThread(runnable);
        }
    }

    public class NotifyRunnable implements Runnable {
        private int idx;
        private int affinity;

        NotifyRunnable(int idx, int affinity){
            this.idx = idx;
            this.affinity = affinity;
        }

        public void run() {
            switch (affinity){
                case 1:
                    if(adapter_days.getItemCount()>=idx)
                        adapter_days.notifyItemChanged(idx);
                    break;
                case 2:
                    if(adapter_month.getItemCount()>=idx)
                        adapter_month.notifyItemChanged(idx);
                    break;
                case 3:
                    if(adapter_months.getItemCount()>=idx)
                        adapter_months.notifyItemChanged(idx);
                    break;
            }

        }
    }

    class CustomOnItemTouchListener implements RecyclerView.OnItemTouchListener {
        int affinity;

        CustomOnItemTouchListener(int affinity){
            this.affinity = affinity;
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child!=null) {

                RecyclerView.ViewHolder viewHolder = rv.findViewHolderForAdapterPosition(rv.getChildLayoutPosition(child));
                int pos = viewHolder.getAdapterPosition();

                if (gestureDetector.onTouchEvent(e)) {
                    if(affinity == 1){
                        if(histories_days.get(pos).getBtn() && isLeft){
                            histories_days.get(pos).setBtn(false);
                            ItemChangeRunnable itemChangeRunnable = new ItemChangeRunnable(affinity, pos);
                            itemChangeRunnable.run();
                        }
                        else if(!isLeft){
                            histories_days.get(pos).setBtn(true);
                            ItemChangeRunnable itemChangeRunnable = new ItemChangeRunnable(affinity, pos);
                            itemChangeRunnable.run();
                        }
                    } else if (affinity == 2){
                        if(histories_month.get(pos).getBtn() && isLeft){
                            histories_month.get(pos).setBtn(false);
                            ItemChangeRunnable itemChangeRunnable = new ItemChangeRunnable(affinity, pos);
                            itemChangeRunnable.run();
                        }
                        else if(!isLeft){
                            histories_month.get(pos).setBtn(true);
                            ItemChangeRunnable itemChangeRunnable = new ItemChangeRunnable(affinity, pos);
                            itemChangeRunnable.run();
                        }
                    } else if (affinity == 3){
                        if(histories_months.get(pos).getBtn() && isLeft){
                            histories_months.get(pos).setBtn(false);
                            ItemChangeRunnable itemChangeRunnable = new ItemChangeRunnable(affinity, pos);
                            itemChangeRunnable.run();
                        }
                        else if(!isLeft){
                            histories_months.get(pos).setBtn(true);
                            ItemChangeRunnable itemChangeRunnable = new ItemChangeRunnable(affinity, pos);
                            itemChangeRunnable.run();
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }

        class ItemChangeRunnable implements Runnable{
            int affinity;
            int pos;

            ItemChangeRunnable(int affinity, int pos){
                this.affinity = affinity;
                this.pos = pos;
            }

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(affinity == 1){
                            adapter_days.notifyItemChanged(pos);
                        } else if(affinity == 2){
                            adapter_month.notifyItemChanged(pos);
                        } else if(affinity == 3){
                            adapter_months.notifyItemChanged(pos);
                        }
                    }
                });
            }
        }
    };

}
