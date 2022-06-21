package com.example.courseworklive.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseworklive.R;
import com.example.courseworklive.adapters.SubjectsAdapter;
import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.dialogFragments.CheckForDeletion;
import com.example.courseworklive.models.Subject;
import com.example.courseworklive.service.UpdateFirebase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;

public class SubjectsPage extends AppCompatActivity implements SubjectsAdapter.OnSubjectListener, CheckForDeletion.CheckForDeletionListener {

    /**
     * In this activity we implement the Subject Adapter Listener
     * and the check for deletion listener in order to avoid accidental subject deletion.
     * Touch gesture support is also included.
     * They are the UI elements as well as the elements needed to set up
     * the Recycler View
     * */
    private ArrayList<Subject> subjects = new ArrayList<>();
    private SubjectsAdapter subjectsAdapter;
    private BottomNavigationView navigation;
    private RecyclerView rvSubjects;
    private ItemTouchHelper touchHelper;

    //primarily used to indicate the position of the item selected in the recycler view
    //as well as to indicate whether or not the next action should be straight to the recoding
    private int position;
    private boolean goToRecording = false;
    private static final String TAG = "SubjectsPage";

    /**
     * This is the swipe support
     * This is used in order to delete a subject that is no longer needed
     * */
    private ItemTouchHelper.SimpleCallback deleteSwipe = new ItemTouchHelper.SimpleCallback(5, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            position = viewHolder.getAdapterPosition();
            switch (direction) {
                case ItemTouchHelper.LEFT:
                    //prompting the user to confirm the deletion
                    CheckForDeletion deletion = new CheckForDeletion("Delete", "Are you sure you want to delete this subject?", "Delete", "Cancel");
                    deletion.setCancelable(false);
                    deletion.show(getSupportFragmentManager(), "Deletion");
                    break;
                case ItemTouchHelper.RIGHT:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects_page);

        //receiving all the relevant information on whether or not to go to recording
        Intent intent = getIntent();
        goToRecording = intent.getBooleanExtra("recording", false);

        //setting the navigation bar listener
        navigation = findViewById(R.id.navigation);
        navigation.setItemIconTintList(null);
        navigation.setOnNavigationItemSelectedListener(navListener);

        //if goToRecording is true this activity works as an intermediary
        if (goToRecording) {
            navigation.setVisibility(View.INVISIBLE);
        }

        //setting up the recycler view
        rvSubjects = findViewById(R.id.listviewSubjects);
        TextView emptyView = findViewById(R.id.empty_view);
        Thread thread = new Thread(){
            @Override
            public void run() {
                super.run();
                subjects = getSubjects();
            }
        };
        thread.run();
        Log.d(TAG, String.valueOf(thread.isAlive()));

        //if the recycler view is empty a message is shown
        if (subjects.isEmpty()) {
            rvSubjects.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvSubjects.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        //if goToRecording is false then all the listeners are connected
        if (!goToRecording) {
            touchHelper = new ItemTouchHelper(deleteSwipe);
            touchHelper.attachToRecyclerView(rvSubjects);
        }
        subjectsAdapter = new SubjectsAdapter(subjects, getApplicationContext(), this);
        rvSubjects.setAdapter(subjectsAdapter);
        // Set layout manager to position the items
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
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
                    subjectsAdapter.notifyDataSetChanged();
                    break;
                case R.id.main_menu_nav_button:
                    Intent a = new Intent(SubjectsPage.this, MainActivity.class);
                    startActivity(a);
                    break;
                case R.id.add_subject_nav_menu:
                    Intent b = new Intent(SubjectsPage.this, AddSubject.class);
                    startActivity(b);
                    break;
            }
            return false;
        }
    };


    /**
     * The method used to load all the subjects rom the database
     * */
    private ArrayList<Subject> getSubjects() {

        ArrayList<Subject> subject = new ArrayList<>();

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
                //Log.d(TAG, "start: "+cursor.getLong(cursor.getColumnIndex(CourseworkContentProvider.startDate)));
                subjectTemp.setStartingDate(calendar);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Subject_Table.endDate)));
                //Log.d(TAG, "end: " + cursor.getLong(cursor.getColumnIndex(CourseworkContentProvider.endDate)));
                subjectTemp.setEndDate(calendar);
                subjectTemp.setTotalHours(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.totalHours)));
                subject.add(subjectTemp);
                cursor.moveToNext();
            }
        }
        cursor.close();

        return subject;
    }

    /**
     * This method is used when a user clicks the card in the recycler view
     * It will then start the SubjectOverview activity and pass the information about the subejcct
     * selected. In case the goToRecording is true it will then start the Recording activity
     * */
    @Override
    public void onItemClick(int item) {
        position = item;

        Bundle bundle = new Bundle();
        bundle.putInt("id", subjects.get(item).getId());
        bundle.putString("title", subjects.get(item).getSubjectTitle());
        bundle.putString("weight", "" + subjects.get(item).getWeight());
        bundle.putString("effort", "" + subjects.get(item).getEffort());
        bundle.putLong("start", subjects.get(item).getStartingDate().getTimeInMillis());
        bundle.putLong("end", subjects.get(item).getEndDate().getTimeInMillis());
        bundle.putString("total", "" + subjects.get(item).getTotalHours());

        if (goToRecording) {
            Intent recording = new Intent(SubjectsPage.this, Recording.class);
            recording.putExtras(bundle);
            startActivity(recording);
        } else {
            Intent recording = new Intent(SubjectsPage.this, SubjectOverview.class);
            recording.putExtras(bundle);
            startActivity(recording);
        }
    }

    /**
     * This is the onPositiveClick method
     * Its main use is in the case that a user clicks delete.
     * It deletes the subject swiped by the user
     * */
    @Override
    public void onPositiveClick(DialogFragment dialogFragment) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                ContentResolver resolver = getContentResolver();
                resolver.delete(DataContract.SUBJECT_URI, DataContract.Subject_Table.id + "=?"
                        , new String[]{String.valueOf(subjects.get(position).getId())});

                resolver.delete(DataContract.LOGS_URI, DataContract.Subject_Table.id + "=?"
                        , new String[]{String.valueOf(subjects.get(position).getId())});

                Bundle b = new Bundle();
                b.putInt("flag", 4);
                b.putString("title", subjects.get(position).getSubjectTitle());
                startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(b));
                subjects.remove(position);
                subjectsAdapter.removeSubject(position);
                subjectsAdapter.notifyDataSetChanged();
                //subjectsAdapter.notifyItemRemoved(position);
            }
        };
        thread.run();

    }

    /**
     * In case the user clicks cancel the adapter is notiied in order to bring back the element
     * */
    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        subjectsAdapter.notifyDataSetChanged();
    }

    /**
     * Setting the listeners from the activity
     * when the activity resumes
     * */
    @Override
    protected void onResume() {
        super.onResume();
        navigation.setOnNavigationItemSelectedListener(navListener);
        if (!goToRecording) {
            touchHelper.attachToRecyclerView(rvSubjects);
        }
        Thread th = new Thread() {
            @Override
            public void run() {
                subjects = getSubjects();
                subjectsAdapter.notifyDataSetChanged();
            }
        };
        th.run();
    }

    /**
     * Removing the listeners from the activity
     * when it is paused
     * */
    @Override
    protected void onPause() {
        super.onPause();
        navigation.setOnNavigationItemSelectedListener(null);
        if (!goToRecording) {
            touchHelper.attachToRecyclerView(null);
        }
    }

    /**
     * Removing the listeners from the activity
     * when it is destroyed
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigation.setOnNavigationItemSelectedListener(null);
        if (!goToRecording) {
            touchHelper.attachToRecyclerView(null);
        }
    }
}