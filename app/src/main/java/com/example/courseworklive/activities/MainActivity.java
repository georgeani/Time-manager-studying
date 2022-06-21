package com.example.courseworklive.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.courseworklive.R;
import com.example.courseworklive.broadcastReceiver.WakeUp;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    /**
    * The variables used to keep the elements present in the MainActivity UI
    * TAG is used for logs
    */
    private static final String TAG = "Main Activity";
    private BottomNavigationView navigation;
    private Button subject;
    private Button dashboard;
    private Button record;

    /**
    * onCreate method: used to instantiate the activity
    * makes sure the listeners are attached to their respected UI element
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //creates the alarm manager that sends periodic notifications
        //and checks how much progress has been made
        Thread thread = new Thread() {
            @Override
            public void run() {
                checkProgress();
            }
        };
        thread.run();

        //makes sure that Firestore is not persistent
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);

        //assigning the UI elements and their listeners
        subject = findViewById(R.id.button_subject_main_menu);
        subject.setOnClickListener(goToSubjectsListener);
        dashboard = findViewById(R.id.button_dashboard_main_menu);
        dashboard.setOnClickListener(goToDashboardListener);
        record = findViewById(R.id.button_recording_main_menu);
        record.setOnClickListener(goToRecordingListener);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        //making sure the navigation bar is working
        navigation = findViewById(R.id.navigation);
        navigation.setItemIconTintList(null);
        navigation.setOnNavigationItemSelectedListener(navListener);
    }

    /**
    * The navigation bar listener that allows the navigation between the main menu,
    * Subject Page and the add a new Subject Page
    */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.subject_nav_button:
                    Intent a = new Intent(MainActivity.this, SubjectsPage.class);
                    a.putExtra("recording", false);
                    startActivity(a);
                    break;
                case R.id.main_menu_nav_button:
                    break;
                case R.id.add_subject_nav_menu:
                    Intent b = new Intent(MainActivity.this, AddSubject.class);
                    startActivity(b);
                    break;
            }
            return false;
        }
    };

    /**
     * The listener used for the Subject button
     * It starts the SubjectOverview  Activity*/
    private View.OnClickListener goToSubjectsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent subjects = new Intent(MainActivity.this, SubjectsPage.class);
            startActivity(subjects);
        }
    };

    /**
     * The listener used for the Dashboard button
     *  It starts the Dashboard Activity*/
    private View.OnClickListener goToDashboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent subjects = new Intent(MainActivity.this, Dashboard.class);
            startActivity(subjects);
        }
    };

    /**
     * The listener used for the Recording button
     * It starts the SubjectOverview Activity and trough clicking in a subject you can
     * go straight to the recording activity*/
    private View.OnClickListener goToRecordingListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intermediary = new Intent(MainActivity.this, SubjectsPage.class);
            intermediary.putExtra("recording", true);
            startActivity(intermediary);
        }
    };

    /**
     * re-instantiates the listeners if an activity is resumed
     * makes sures that it loads to the last activity that the user exited from*/
    @Override
    protected void onResume() {

        navigation.setOnNavigationItemSelectedListener(navListener);
        subject.setOnClickListener(goToSubjectsListener);
        dashboard.setOnClickListener(goToDashboardListener);
        record.setOnClickListener(goToRecordingListener);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("SubjectPreSave", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("saved", false)){
            startActivity(new Intent(MainActivity.this, AddSubject.class));
        }

        super.onResume();
    }

    //used to remove the listeners when the activity is put on pause
    @Override
    protected void onPause() {
        navigation.setOnNavigationItemSelectedListener(null);
        subject.setOnClickListener(null);
        dashboard.setOnClickListener(null);
        record.setOnClickListener(null);
        super.onPause();
    }

    //ensuring that listeners have been removed before the activity is destroyed
    @Override
    protected void onDestroy() {
        navigation.setOnNavigationItemSelectedListener(null);
        subject.setOnClickListener(null);
        dashboard.setOnClickListener(null);
        record.setOnClickListener(null);
        super.onDestroy();
    }

    /**
     * used to create an options menu
     * that menu contains the user guide and the login functionality to use the Firebase*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * used to select the appropriate activity
     * in this case it is to go to the Login activity for Firestore
     * as well as the user guide*/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_in:
                Log.d("Main", "Sign In");
                startActivity(new Intent(this, LoginToFirebase.class));
                return true;
            case R.id.guide:
                Log.d("Main", "Guide");
                startActivity(new Intent(this, UserGuide.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * method used in order to create the alarm that activates the notification function as well as
     * checks whether or not to send a notification regarding the results and how much a user has
     * worked to their studies*/
    private void checkProgress() {

            Intent intent = new Intent(this, WakeUp.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this.getApplicationContext(), 234324243, intent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + (TimeUnit.SECONDS.toMillis(1)), pendingIntent);

    }

}