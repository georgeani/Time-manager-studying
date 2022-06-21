package com.example.courseworklive.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.courseworklive.R;
import com.example.courseworklive.activities.MainActivity;
import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.models.Subject;

import java.util.ArrayList;
import java.util.Calendar;

public class CheckSubjectsDeadline extends Service {

    /**
     * CheckSubjectDeadline is a service that checks whether the user is not studying enough
     * It does that by retrieving the data from the database content provider
     * and parses it into Subject objects. It then uses the checkProgress to check the progress
     * If at least one subject does not meet the criteria.
     * A notification is sent that tells the user to that they are not studying enough
     * */
    public CheckSubjectsDeadline() {
    }

    private ArrayList<Subject> subjects = new ArrayList<>();
    private NotificationCompat.Builder builder;
    private String CHANNEL_ID = "1234";
    private NotificationManager notificationManager;

    /**
     * Setting up the notification that snoozes the user
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Intent notificationIntent = new Intent(this, MainActivity.class);

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_book)
                .setContentTitle("You are falling behind")
                .setContentText("You are falling behind some of your subjects")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You are falling behind some of your subjects"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Log.d("ServiceDead", "Setup");

        builder.setContentIntent(pendingIntent).setAutoCancel(true);
    }

    /**
     * Starts the service
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                //retrieves the subject data
                getSubjects();
                for (Subject subject:subjects){
                    //checks that the subject threshold is met
                    if (subject.checkProgress(System.currentTimeMillis())){
                        //if it is loop stops and the notification is sent
                        setUpNotification(getApplicationContext());
                        notificationManager.notify(5, builder.build());
                        break;
                    }
                }
                //service stops
                onDestroy();
            }
        };
        thread.run();

        return START_STICKY;
    }

    /**
     * Retrieves all the subjects from the database
     * */
    private void getSubjects(){
        Cursor cursor = getContentResolver().query(DataContract.SUBJECT_URI, null, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            Subject subjectTemp;
            while (!cursor.isAfterLast()) {
                Calendar calendar = Calendar.getInstance();
                subjectTemp = new Subject();
                subjectTemp.setId(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.id)));
                subjectTemp.setSubjectTitle(cursor.getString(cursor.getColumnIndex(DataContract.Subject_Table.name)));
                subjectTemp.setWeight(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.weight)));
                subjectTemp.setEffort(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.effort)));
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Subject_Table.startDate)));
                subjectTemp.setStartingDate(calendar);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Subject_Table.endDate)));
                subjectTemp.setEndDate(calendar);
                subjectTemp.setTotalHours(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.totalHours)));
                subjects.add(subjectTemp);
                cursor.moveToNext();
            }
        }
        cursor.close();
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
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
