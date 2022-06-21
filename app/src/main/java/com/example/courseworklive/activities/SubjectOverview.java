package com.example.courseworklive.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseworklive.R;
import com.example.courseworklive.adapters.TimingAdapter;
import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.dialogFragments.CheckForDeletion;
import com.example.courseworklive.models.Logs;
import com.example.courseworklive.models.Subject;
import com.example.courseworklive.service.UpdateFirebase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class SubjectOverview extends AppCompatActivity implements TimingAdapter.OnTimingListener, CheckForDeletion.CheckForDeletionListener {

    /**
     * In this activity we implement the Timing Adapter Listener
     * and check for deletion listener.
     * This is done as swipe actions are present in the recycler view used and their main
     * purpose is to delete logs that are wrong.
     * These are the UI elements as well as the elements needed to set up
     * the Recycler View and add swipe support.
     * It should be noted that the check for deletion listener is primarily used
     * in order to avoid accidental deletions.
     * */
    private ArrayList<Logs> logs = new ArrayList<>();
    private TimingAdapter timingAdapter;
    private RecyclerView rvSubjectLogs;
    private int position;
    private boolean deleteSubject = false;
    private Calendar startDay;
    private Calendar endDay;

    private ImageButton modify;
    private ImageButton delete;
    private ImageButton addRecording;
    private ItemTouchHelper itemTouchHelper;

    private TextView title;
    private TextView weight;
    private TextView effort;
    private TextView start;
    private TextView end;
    private TextView total;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_overview);

        //connecting to the elements in the UI
        //and setting up the listeners
        delete = findViewById(R.id.imageButtonDeleteSubjectOverview);
        delete.setOnClickListener(deleteListener);
        modify = findViewById(R.id.imageButtonModifySubjectOverview);
        modify.setOnClickListener(modifyListener);
        addRecording = findViewById(R.id.imageButtonSubjectOverview);
        addRecording.setOnClickListener(startRecording);

        //receiving the data for the relevant subject
        //and modifying the UI to present the data
        Intent intent = getIntent();
        bundle = intent.getExtras();
        title = findViewById(R.id.subjectOverviewTitle);
        title.setText(bundle.getString("title"));
        weight = findViewById(R.id.weightSubjectOverviewChangeable);
        weight.setText(bundle.getString("weight"));
        effort = findViewById(R.id.EffortSubjectOverviewChangeable);
        effort.setText(bundle.getString("effort"));
        startDay = Calendar.getInstance();
        startDay.setTimeInMillis(bundle.getLong("start"));
        start = findViewById(R.id.StartSubjectOverviewChangeable);
        start.setText(Subject.getDate(startDay));
        end = findViewById(R.id.EndSubjectOverviewChangeable);
        endDay = Calendar.getInstance();
        endDay.setTimeInMillis(bundle.getLong("end"));
        end.setText(Subject.getDate(endDay));
        total = findViewById(R.id.TotalHoursSubjectOverviewChangeable);
        long temp = Long.parseLong(Objects.requireNonNull(bundle.getString("total")));
        total.setText(Logs.getTimeSpentHours(temp));

        //setting up the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        //receiving the logs or the recycler view
        Thread thread = new Thread() {
            @Override
            public void run() {
                logs = getLogs(bundle.getInt("id"));
            }
        };
        thread.run();

        //setting up the recycler view
        rvSubjectLogs = findViewById(R.id.overviewRecycler);

        TextView empty = findViewById(R.id.empty_view_overview);

        if (logs.isEmpty()){
            rvSubjectLogs.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        } else {
            rvSubjectLogs.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
        }

        //adding the swipe support
        itemTouchHelper = new ItemTouchHelper(swipeAction);
        itemTouchHelper.attachToRecyclerView(rvSubjectLogs);

        timingAdapter = new TimingAdapter(logs, this);
        rvSubjectLogs.setAdapter(timingAdapter);
        rvSubjectLogs.setLayoutManager(new LinearLayoutManager(this));

    }

    /**
     * This is the swipe support
     * This is used in order to delete a log that is no longer needed
     * This is in order to remove logs that do not need to be counted and discarded
     * possibly due to being a falsely triggered event
     * */
    ItemTouchHelper.SimpleCallback swipeAction = new ItemTouchHelper.SimpleCallback(5, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            position = viewHolder.getAdapterPosition();
            switch (direction){
                case ItemTouchHelper.LEFT:
                    //when a log is to be deleted the user
                    // will be prompted to agree that to delete the log
                    CheckForDeletion deletion = new CheckForDeletion("Deletion","Do you want to delete the following?", "Delete", "Cancel");
                    deletion.setCancelable(false);
                    deletion.show(getSupportFragmentManager(),"Deletion");
                    break;
                case ItemTouchHelper.RIGHT:
                    deletion = new CheckForDeletion("Deletion","Do you want to delete the following?", "Delete", "Cancel");
                    deletion.setCancelable(false);
                    deletion.show(getSupportFragmentManager(),"Deletion");
                    break;
            }
        }
    };

    /**
     * The listener for the delete action. This listener will
     * prompt the user respond if the subject should be deleted.
     * */
    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            CheckForDeletion deletion = new CheckForDeletion("Delete Subject","Do you want to delete this subject?", "Delete", "Cancel");
            deletion.setCancelable(false);
            deleteSubject = true;
            deletion.show(getSupportFragmentManager(),"Deletion");
        }
    };

    /**
     * This listener will open the AddSubject activity in such a way in
     * order to modify the subject in the database.
     * As well as update the Firebase with the newest information
     * */
    private View.OnClickListener modifyListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences sharedPreferences = getSharedPreferences("SubjectPreSave", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("id", bundle.getInt("id"));
            editor.putBoolean("saved", true);
            editor.putString("subject", title.getText().toString());
            editor.putString("weight", weight.getText().toString());
            editor.putString("effort", effort.getText().toString());
            editor.putString("start", start.getText().toString());
            editor.putString("end", end.getText().toString());
            editor.putBoolean("bundle", true);
            editor.apply();
            startActivity(new Intent(SubjectOverview.this, AddSubject.class).putExtras(bundle));
        }
    };

    @Override
    public void onItemClick(int item) {
        //Possible future extension to see more info for a recording
    }

    /**
     * This listener will open the Recording activity as well as passing all the relevant
     * information.
     * */
    private View.OnClickListener startRecording = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(SubjectOverview.this, Recording.class).putExtras(bundle));
        }
    };

    /**
     * getLogs method is used in order to retrieve all the logs for a specific subject
     */
    private ArrayList<Logs> getLogs(int subjectID){
        ArrayList<Logs> logs = new ArrayList<>();

        String selection = DataContract.Subject_Table.id + "=" + subjectID;

        //running the query
        Cursor cursor = getContentResolver().query(DataContract.LOGS_URI, null, selection, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            Logs log;
            while (!cursor.isAfterLast()) {
                Calendar calendar = Calendar.getInstance();
                log = new Logs();
                log.setId(cursor.getInt(cursor.getColumnIndex(DataContract.Log_Table.id)));
                log.setTimeSpent(cursor.getInt(cursor.getColumnIndex(DataContract.Log_Table.length)));
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Log_Table.date)));
                log.setDate(calendar);
                logs.add(log);
                cursor.moveToNext();
            }
        }
        cursor.close();

        return logs;
    }

    /**
     * This is the popup menu
     * It is used to indicate which action should be chosen
     * in this case it allows to set the starting
     * and ending date in the calendar
     * */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.calendar_start:
                addStartDate();
                return true;
            case R.id.ending_day_calendar:
                addEndDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * This is the positive click action from the click for deletion listener
     * It is activated if the user clicks Delete in the prompt
     * It allows for both the deletion of the subject as well as the deletion
     * of individual logs
     * */
    @Override
    public void onPositiveClick(DialogFragment dialogFragment) {
        if (deleteSubject){
            //deleting the subject
            //this is also done in the Firebase
            //through updating it
            startActivity(new Intent(SubjectOverview.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            deleteSubject = false;
            Thread th = new Thread(){
                @Override
                public void run() {
                    super.run();
                    Bundle b = new Bundle();
                    b.putInt("flag", 4);
                    b.putString("title", bundle.getString("title"));
                    startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(b));
                    getContentResolver().delete(DataContract.SUBJECT_URI, DataContract.Subject_Table.id + "=?"
                            , new String[]{String.valueOf(bundle.getInt("id"))});
                    Log.d("Delete", String.valueOf(bundle.getInt("id")));
                    getContentResolver().delete(DataContract.LOGS_URI, DataContract.Log_Table.id + "=? ", new String[]{String.valueOf(bundle.getInt("id"))});
                }
            };
            th.run();
        } else {
            //this is for the deletion of logs
            //this also updates Firebase with the correct information
            //thus keeping it up to date
            getContentResolver().delete(DataContract.LOGS_URI, DataContract.Log_Table.id + "=? "+
                    "AND " + DataContract.Log_Table.date + "=?", new String[]{String.valueOf(bundle.getInt("id")), String.valueOf(logs.get(position).getDate().getTimeInMillis())});
            Thread th = new Thread(){
                @Override
                public void run() {
                    super.run();
                    updateHours(position);
                }
            };
            th.run();
            logs.remove(position);
            timingAdapter.notifyItemRemoved(position);

        }
    }

    /**
     * This is the method used in order to update the database with with the correct time as well
     * as updating Firebase
     * */
    private void updateHours(int position){
        //updating the database
        ContentValues values = new ContentValues();
        values.put(DataContract.Subject_Table.name, title.getText().toString());
        values.put(DataContract.Subject_Table.weight, weight.getText().toString());
        values.put(DataContract.Subject_Table.effort, effort.getText().toString());
        values.put(DataContract.Subject_Table.startDate, bundle.getLong("start"));
        //Log.d(TAG, startCal.getTimeInMillis() + "");
        //Log.d(TAG, "" + endCal.getTimeInMillis());
        values.put(DataContract.Subject_Table.endDate, bundle.getLong("end"));
        values.put(DataContract.Subject_Table.totalHours, Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) - logs.get(position).getTimeSpent());
        getContentResolver().update(DataContract.SUBJECT_URI,
                values, DataContract.Subject_Table.id + "=?", new String[]{String.valueOf(bundle.getInt("id"))});
        total.setText(Logs.getTimeSpentHours(Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) - logs.get(position).getTimeSpent()));
        //updating firebase through UpdateFirebase Service
        Bundle b = new Bundle();
        b.putInt("flag", 3);
        b.putString("title", bundle.getString("title"));
        b.putString("total", String.valueOf(Long.parseLong(Objects.requireNonNull(bundle.getString("total"))) - logs.get(position).getTimeSpent()));
        startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(b));
    }

    //making sure that if the user has declined to make the deletion the recycler view is up to date
    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        if(!deleteSubject){
            timingAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Making sure the recycler view is up to date
     * and the listeners are attached
     * */
    @Override
    protected void onResume() {
        super.onResume();
        delete.setOnClickListener(deleteListener);
        modify.setOnClickListener(modifyListener);
        addRecording.setOnClickListener(startRecording);
        itemTouchHelper.attachToRecyclerView(rvSubjectLogs);
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                logs = getLogs(bundle.getInt("id"));
            }
        };
        thread.run();
        timingAdapter.newDataSet(logs);
        timingAdapter.notifyDataSetChanged();
    }

    /**
     * Making sure that the listeners are disconnected from the UI when the activity is paused
     * */
    @Override
    protected void onPause() {
        delete.setOnClickListener(null);
        modify.setOnClickListener(null);
        addRecording.setOnClickListener(null);
        itemTouchHelper.attachToRecyclerView(null);
        super.onPause();
    }

    /**
     * Making sure that the listeners are disconnected from the UI when the activity is destroyed
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        delete.setOnClickListener(null);
        modify.setOnClickListener(null);
        addRecording.setOnClickListener(null);
        itemTouchHelper.attachToRecyclerView(null);
    }

    /**
     * Making the popup menu as well as creating the ShareAction Provider
     * The share action provider is used to share the essential information of the subject
     * currently in the page though the an app that supports plain text
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actions_subject, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        share(actionProvider);
        return true;
    }

    /**
     * The share method used to create the string that passes the data
     * */
    private void share(ShareActionProvider actionProvider){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String s = "Title: " + title.getText() + "\n" +
                "Start Date: " + start.getText() + "\n" +
                "End Date: " + end.getText() + "\n" +
                "Effort: " + effort.getText() +"\n" +
                "Weight: " + weight.getText() + "\n" +
                "Total hours: " + Logs.getTimeSpentMins(Long.parseLong(Objects.requireNonNull(bundle.getString("total"))));
        intent.putExtra(Intent.EXTRA_TEXT, s);
        actionProvider.setShareIntent(intent);
    }

    /**
     * Method used to add the start date in the calendar as well as some relevant information
     * */
    private void addStartDate(){
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, bundle.getString("title") + " Start");
        String stringBuilder = "Weight: " +
                weight.getText() +
                "\n" +
                "Effort: " +
                effort.getText() + " hours";
        intent.putExtra(CalendarContract.Events.DESCRIPTION, stringBuilder);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDay.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.ALL_DAY, true);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No App Support", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method used to add the ending date in the calendar as well as some relevant information
     * */
    private void addEndDate(){
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, bundle.getString("title") + " End");
        String stringBuilder = "Weight: " +
                weight.getText() +
                "\n" +
                "Effort: " +
                effort.getText() + " hours";
        intent.putExtra(CalendarContract.Events.DESCRIPTION, stringBuilder);
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, endDay.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.ALL_DAY, true);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No App Support", Toast.LENGTH_SHORT).show();
        }

    }


}