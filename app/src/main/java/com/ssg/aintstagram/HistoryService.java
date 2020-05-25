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
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.app.NotificationManager.*;

public class HistoryService extends Service {
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
        showNotification();
        Log.e("SERVICE", "DEBUG");
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
    private void showNotification(){
        CharSequence text = "HelloWorld";

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
}
