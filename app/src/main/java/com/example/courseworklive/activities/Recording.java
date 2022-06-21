package com.example.courseworklive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.courseworklive.R;
import com.example.courseworklive.service.RecordService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Recording extends AppCompatActivity {

    /**
     * In this activity we start the recording sessions for the subjects
     * The following are the variables used.
     * They are the UI elements needed for the activity to work
     * */
    private Intent service;
    private ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        //receiving the intent and all the relevant information
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        service = new Intent(this, RecordService.class);
        assert bundle != null;
        service.putExtras(bundle);
        //starting the recording service
        startService(service);
        //setting up the UI with all the relevant information
        TextView title = findViewById(R.id.titleRecording);
        title.setText(bundle.getString("title"));
        TextView startingTime = findViewById(R.id.startingTimeRecording);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault());
        startingTime.setText(format.format(Calendar.getInstance().getTime()));

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        //setting up the listener
        button = findViewById(R.id.saveButtonRecording);
        button.setOnClickListener(saveListener);

    }

    /**
     * The listener used to stop the recording
     * It removes the listener and exits from the activity
     * it also stops the recording service
     * */
    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            stopService(service);
            button.setOnClickListener(null);
            finish();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    };

    /**
     * In the case the back button is pressed the service is stopped and the
     * listeners are removed
     * */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        button.setOnClickListener(null);
        this.finish();
        stopService(service);
    }

    /**
     * When the activity is paused
     * listeners are removed
     * */
    @Override
    protected void onPause() {
        super.onPause();
        button.setOnClickListener(null);
    }

    /**
     * When the activity is resumed
     * listeners are attached again
     * */
    @Override
    protected void onResume() {
        super.onResume();
        button.setOnClickListener(saveListener);
    }

    /**
     * In the case the back button in the action bar
     * is pressed the service is stopped and the
     * listeners are removed
     * */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            stopService(service);
            button.setOnClickListener(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}