package com.example.courseworklive.broadcastReceiver;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.courseworklive.R;
import com.example.courseworklive.activities.MainActivity;
import com.example.courseworklive.service.CheckSubjectsDeadline;

import java.util.concurrent.TimeUnit;

public class WakeUp extends BroadcastReceiver {

    /**
     * This Broadcast Receiver is created in such a way that periodically activates
     * and sends notifications to the user to snooze them in order to use the app
     * it also activates the service that checks whether or not the user is studying enough
     * */
    private NotificationManager notificationManager;
    private String CHANNEL_ID = "1234";

    @Override
    public void onReceive(Context context, Intent intent) {

        //creating the notification channel in order to work with newer Android Versions
        setUpNotification(context);
        //starting the service that checks the subject deadlines
        Intent service = new Intent(context, CheckSubjectsDeadline.class);
        context.startService(service);

        //making the notification to snooze the user to use the app
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_book)
                .setContentTitle("Time to study")
                .setContentText("Time to start working on your subjects")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Time to start working on your subjects"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent2 = new Intent(context, MainActivity.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent2, 0);

        builder.setContentIntent(pendingIntent).setAutoCancel(true);

        notificationManager.notify(1, builder.build());

        //setting the alarm to activate the broadcaster in one day
        Intent intent3 = new Intent(context, WakeUp.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
                context, 234324243, intent3, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                + (TimeUnit.DAYS.toMillis(1)), pendingIntent2);

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

}
