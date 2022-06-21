package com.example.courseworklive.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.courseworklive.R;
import com.example.courseworklive.activities.Recording;
import com.example.courseworklive.contentProvider.DataContract;

import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class RecordService extends Service {

    /**
     * RecordService is a service that records the user's studying sessions
     * The method is build in such a way that it only runs for a maximum of 8 hours
     * When it is about to close the service, it sends a notification to thee user
     * that the service is about to be terminated. It is a foreground service
     * meaning that the user is aware that it is running.
     * The service is also terminated if the user force closes the application.
     * */
    private long start;
    private long end;
    private Bundle bundle;
    private String CHANNEL_ID = "1234";
    private NotificationCompat.Builder builder;
    private int id;
    private static Timer timer = new Timer();
    private NotificationManager notificationManager;

    /**
     * Setting up the notification that notifies the user that the
     * application is running
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, Recording.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_book)
                .setContentTitle("Recording Started")
                .setContentText("Currently recording a subject")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Currently recording a subject"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Log.d("Service", "Setup");

        builder.setContentIntent(pendingIntent).setAutoCancel(true);
    }

    public RecordService() {
    }

    /**
     * Starts the service
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //setting the starting and ending parameters
        start = System.currentTimeMillis();
        end = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(8);
        bundle = intent.getExtras();
        assert bundle != null;
        id = bundle.getInt("id");

        startForeground(1, builder.build());
        timer.scheduleAtFixedRate(new Timed(), 0, TimeUnit.MINUTES.toMillis(5));

        Log.d("Service", "Start");

        return START_STICKY;
    }

    /**
     * The timer class that checks how long has the service run
     * */
    private class Timed extends TimerTask{

        @Override
        public void run() {
            if ((end - TimeUnit.MINUTES.toMillis(5)) == System.currentTimeMillis()){
                //if it is close to the 5 minute mark it will send a closing notification
                almostEndingNotification();
            } else if (end >= System.currentTimeMillis()){
                onDestroy();
            }
        }
    }

    /**
     * The notification that notifies that the service is ending
     * */
    private void almostEndingNotification(){
        Intent notificationIntent = new Intent(this, Recording.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_book)
                .setContentTitle("Recording Ends Soon")
                .setContentText("This subject recording will end in 5 minutes")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("This subject recording will end in 5 minutes"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder1.setContentIntent(pendingIntent).setAutoCancel(true);

        setUpNotification(getApplicationContext());
        notificationManager.notify(1, builder1.build());
    }

    /**
     * The notification that notifies that the service has ended
     * */
    private void endingNotification(){

        Intent notificationIntent = new Intent(this, Recording.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_book)
                .setContentTitle("Recording Ended")
                .setContentText("This subject recording has ended")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("This subject recording has ended"))
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        builder1.setContentIntent(pendingIntent).setAutoCancel(true);

        setUpNotification(getApplicationContext());
        notificationManager.notify(1, builder1.build());

    }

    /**
     * Making sure that the time is registered
     * including a log entry as well as updating Firebase
     * when the service stops
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (System.currentTimeMillis() - start >= 1000){
            timer.cancel();
            timer.purge();
            ContentValues values = new ContentValues();
            values.put(DataContract.Log_Table.id, id);
            values.put(DataContract.Log_Table.date, Calendar.getInstance().getTimeInMillis());
            values.put(DataContract.Log_Table.length, System.currentTimeMillis() - start);
            getContentResolver().insert(DataContract.LOGS_URI, values);
            ContentValues values1 = new ContentValues();
            values1.put(DataContract.Subject_Table.totalHours, Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) + System.currentTimeMillis() - start);
            getContentResolver().update(DataContract.SUBJECT_URI,
                    values1, DataContract.Log_Table.id + "=?", new String[]{String.valueOf(id)});
            Bundle b = new Bundle();
            b.putInt("flag", 3);
            b.putInt("id", bundle.getInt("id"));
            b.putString("title", bundle.getString("title"));
            b.putString("total", String.valueOf(Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) + System.currentTimeMillis() - start));
            startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(b));
            endingNotification();
        }
        Log.d("Service", "End");
        Log.d("Service", String.valueOf(System.currentTimeMillis() - start));
    }

    /**
     * Making sure that the time is registered
     * including a log entry as well as updating Firebase
     * when the service force closed
     * */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (System.currentTimeMillis() - start > 1000){
            timer.cancel();
            timer.purge();
            ContentValues values = new ContentValues();
            values.put(DataContract.Log_Table.id, id);
            values.put(DataContract.Log_Table.date, Calendar.getInstance().getTimeInMillis());
            values.put(DataContract.Log_Table.length, System.currentTimeMillis() - start);
            getContentResolver().insert(DataContract.LOGS_URI, values);
            ContentValues values1 = new ContentValues();
            values1.put(DataContract.Subject_Table.totalHours, Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) + System.currentTimeMillis() - start);
            getContentResolver().update(DataContract.SUBJECT_URI,
                    values1, DataContract.Log_Table.id + "=?", new String[]{String.valueOf(id)});
            Bundle b = new Bundle();
            b.putInt("flag", 3);
            b.putString("title", bundle.getString("title"));
            b.putString("total", String.valueOf(Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) + System.currentTimeMillis() - start));
            startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(b));

            endingNotification();
        }

    }

    /**
     * Setting up the notification manager
     * This setup makes it possible to be compatible with different android versions
     * */
    private void setUpNotification(Context context) {
        notificationManager = context.getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence notificationName = context.getString(R.string.channel_name);
            String notificationDescription = "Test";
            int importanceLevel = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    notificationName, importanceLevel);
            notificationChannel.setDescription(notificationDescription);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
