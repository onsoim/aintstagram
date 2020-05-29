package com.ssg.aintstagram;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.kakao.auth.Session;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;

import static android.app.NotificationManager.*;

public class HistoryService extends Service implements Runnable{
    private NotificationManager mNM;

    private int NOTIFICATION = 5;

    public class LocalBinder extends Binder {
        HistoryService getService(){
            return HistoryService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        mNM  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification("Welcome to aintstagram");

        Thread mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mNM.cancel(NOTIFICATION);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showNotification(CharSequence text){
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationChannel channel = new NotificationChannel("aintstagram", text, IMPORTANCE_DEFAULT);
        mNM.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "aintstagram")
                .setSmallIcon(R.drawable.aintstagram)
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("aintstagram")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(intent)
                .build();

        mNM.notify(NOTIFICATION, notification);
    }

    public void updateHistory(int record){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final Update_history_seenMutation uh = Update_history_seenMutation.builder().accessToken(Token).record(record).build();
        apolloClient.mutate(uh).enqueue(new ApolloCall.Callback<Update_history_seenMutation.Data>() {
            public void onResponse(@NotNull Response<Update_history_seenMutation.Data> response) {

            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    public void checkHistory(){
        final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        final ApolloClient apolloClient = ApolloClient.builder().serverUrl(getString(R.string.api_url)).okHttpClient(okHttpClient).build();
        String Token = Session.getCurrentSession().getTokenInfo().getAccessToken();

        final HistoryTypeQuery h = HistoryTypeQuery.builder().accessToken(Token).build();
        apolloClient.query(h).enqueue(new ApolloCall.Callback<HistoryTypeQuery.Data>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NotNull Response<HistoryTypeQuery.Data> response) {
                int cnt = response.data().histories.size();
                for(int i=0; i<cnt; i++){
                    Integer record = Integer.parseInt(response.data().histories.get(i).historyId);
                    if (!response.data().histories.get(i).seen) {
                        switch (response.data().histories.get(i).type){
                            case C:
                                showNotification("새로운 댓글이 있습니다.");
                                updateHistory(record);
                                break;
                            case F:
                                showNotification("새로운 팔로우가 추가되었습니다.");
                                updateHistory(record);
                                break;
                            case L:
                                showNotification("새로운 좋아요 정보가 있습니다.");
                                updateHistory(record);
                                break;
                            case $UNKNOWN:
                                break;
                        }
                    }


                }
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
    }

    @Override
    public void run() {
        while(true){
            try{
                checkHistory();
                Thread.sleep(10000);
            } catch(Exception e){

            }
        }
    }

}
