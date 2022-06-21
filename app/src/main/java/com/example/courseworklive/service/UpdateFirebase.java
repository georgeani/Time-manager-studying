package com.example.courseworklive.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.contentProvider.DatabaseHelper;
import com.example.courseworklive.models.Subject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UpdateFirebase extends Service {

    /**
     * UpdateFirebase is a service that updates Firebase with the
     * latest data. Keeping it up to date
     * The service also performs a data reset as well as downloading the data from an existing
     * account to the phone.
     * */
    private static final String TAG = "Firebase Download";
    private Bundle data;
    private FirebaseFirestore remoteDB;
    private ArrayList<Subject> subjects = new ArrayList<>();
    private String username;

    public UpdateFirebase() {
    }

    /**
     * Setting up the Firebase connection
     * Retrieving the data from loginInfo SharedPreference
     * This is done in order to ensure that the user is registered and as such the data can
     * be backed up safely.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "start service");
        remoteDB = FirebaseFirestore.getInstance();
        SharedPreferences shared = getSharedPreferences("loginInfo", MODE_PRIVATE);
        username = shared.getString("username",null);
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        remoteDB.setFirestoreSettings(settings);

    }

    /**
     * Starts the service
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        assert username != null;
        Log.d(TAG, "onStartCommand: " + username);
        // checking if the username is null
        //iff so the service stops
        if (username == null){
            onDestroy();
            stop();
            Log.d(TAG, "onStartCommand: Aborted");
        } else {
            //checking which functionality to use
            data = intent.getExtras();
            assert data != null;
            int flag = data.getInt("flag");

            switch (flag){
                case 1:
                    //upload backup part of the operation
                    Thread t = new Thread(){
                        @Override
                        public void run() {
                            Log.d(TAG, "start update");
                            super.run();
                            Subject subject = new Subject();
                            subject.setId(data.getInt("id"));
                            subject.setSubjectTitle(data.getString("title"));
                            subject.setWeight(Integer.parseInt(Objects.requireNonNull(data.getString("weight"))));
                            subject.setEffort(Integer.parseInt(Objects.requireNonNull(data.getString("effort"))));
                            Calendar startDay = Calendar.getInstance();
                            startDay.setTimeInMillis(data.getLong("start"));
                            subject.setStartingDate(startDay);
                            Calendar endDay = Calendar.getInstance();
                            endDay.setTimeInMillis(data.getLong("end"));
                            subject.setEndDate(endDay);
                            subject.setTotalHours(Integer.parseInt(Objects.requireNonNull(data.getString("total"))));
                            backUpData(subject);
                        }
                    };
                    t.run();
                    onDestroy();
                    break;
                case 2:
                    //download backup part of the operation && system reset
                    systemReset();
                    Thread th = new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            subjects.clear();
                            downloadData();
                        }
                    };
                    th.run();
                    onDestroy();
                    break;
                case 3:
                    //update time
                    Log.d(TAG, "onStartCommand: Update Time");
                    Log.d(TAG, "onStartCommand: id " + data.get("id"));
                    updateTime(data.getString("title"), data.getInt("id"), Integer.parseInt(Objects.requireNonNull(data.getString("total"))));
                    onDestroy();
                    break;
                case 4:
                    Log.d(TAG, "start delete");
                    deleteData(data.getString("title"));
                    onDestroy();
                    break;
                case 5:
                    //add new data
                    Thread t1 = new Thread(){
                        @Override
                        public void run() {
                            Log.d(TAG, "start addition");
                            super.run();
                            Subject subject = new Subject();
                            //subject.setId(data.getInt("id"));
                            subject.setSubjectTitle(data.getString("title"));
                            subject.setWeight(Integer.parseInt(Objects.requireNonNull(data.getString("weight"))));
                            subject.setEffort(Integer.parseInt(Objects.requireNonNull(data.getString("effort"))));
                            Calendar startDay = Calendar.getInstance();
                            startDay.setTimeInMillis(data.getLong("start"));
                            subject.setStartingDate(startDay);
                            Calendar endDay = Calendar.getInstance();
                            endDay.setTimeInMillis(data.getLong("end"));
                            subject.setEndDate(endDay);
                            subject.setTotalHours(Integer.parseInt(Objects.requireNonNull(data.getString("total"))));
                            addData(subject);
                        }
                    };
                    t1.run();
                    onDestroy();
                    break;
                default:
                    break;
            }

            onDestroy();

        }


        return START_NOT_STICKY;
    }

    /**
     * Stopping the service
     * */
    private void stop(){
        stopService(new Intent(getApplicationContext(), UpdateFirebase.class));
    }

    /**
     * Updating that a subject is deleted
     * */
    private void deleteData(String title){
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        remoteDB.collection(username+"Subjects").document(title).update(data);
    }

    /**
     * Updating a subject's time
     * */
    private void updateTime(String title, int id, int total){
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("deleted", false);
        data.put("id", id);
        remoteDB.collection(username+"Subjects").document(title).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                stop();
            }
        });
    }

    /**
     * Adding a new subject to Firebase
     * */
    private void addData(Subject subject){
        remoteDB.collection(username + "Subjects").document(subject.getSubjectTitle()).set(subject.addNewData()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                stop();
            }
        });
        onDestroy();
    }

    /**
     * Updating a subject's information
     * */
    private void backUpData(Subject subject){
        remoteDB.collection(username+"Subjects").document(subject.getSubjectTitle()).set(subject.getDataForFirestore()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                stop();
            }
        });
        onDestroy();
    }

    /**
     * Downloading backed up data to the device*/
    private void downloadData(){

        Log.d(TAG, "download data");

        remoteDB.collection(username+"Subjects").whereEqualTo("deleted", false).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {


                Log.d(TAG, "Listener");
                Log.d(TAG, String.valueOf(Objects.requireNonNull(task.getResult()).size()));
                if (task.isSuccessful()) {
                    //saving data in the an arraylist in Subject objects
                    for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData() + " testsub");
                            Subject subject = new Subject();
                            subject.setSubjectTitle(document.getId());
                            subject.setId(Integer.parseInt(document.getLong("id") + ""));
                            subject.setTotalHours(Integer.parseInt(document.getLong("total") + ""));
                            subject.setEffort(Integer.parseInt(document.getLong("effort") + ""));
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(document.getLong("startingDate"));
                            subject.setStartingDate(cal);
                            cal = Calendar.getInstance();
                            cal.setTimeInMillis(document.getLong("endDate"));
                            subject.setEndDate(cal);
                            subjects.add(subject);
                            Log.d(TAG, "Download size " + subjects.size());
                    }
                    Log.d(TAG, "Success task");
                    Log.d(TAG, String.valueOf(task.getResult().size()));
                    installData();
                    onDestroy();
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });

    }

    /**
     * Reseting the system data by dropping the database tables and remaking them
     * */
    private void systemReset(){
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase database = db.getWritableDatabase();
        database.execSQL("DROP TABLE IF EXISTS " + DataContract.Subject_Table.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + DataContract.Log_Table.TABLE_NAME);
        db.onCreate(database);
    }

    /**
     * Installing the data in the local database
     * */
    private void installData(){

        Log.d(TAG, "start install1");
        Log.d(TAG, "start install1"+ subjects.size());

        //saving the data from the arraylist to the local database
        for (Subject subject : subjects){
            Log.d(TAG, "start install");

            ContentValues values = new ContentValues();
            values.put(DataContract.Subject_Table.id, subject.getId());
            values.put(DataContract.Subject_Table.name, subject.getSubjectTitle());
            values.put(DataContract.Subject_Table.weight, subject.getWeight());
            values.put(DataContract.Subject_Table.effort, subject.getEffort());
            values.put(DataContract.Subject_Table.startDate, subject.getStartingDate().getTimeInMillis());
            //Log.d(TAG, startCal.getTimeInMillis() + "");
            //Log.d(TAG, "" + endCal.getTimeInMillis());
            values.put(DataContract.Subject_Table.endDate, subject.getEndDate().getTimeInMillis());
            values.put(DataContract.Subject_Table.totalHours, subject.getTotalHours());
            getContentResolver().insert(DataContract.SUBJECT_URI, values);

        }
        onDestroy();
        stop();


        Log.d(TAG, "done");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
