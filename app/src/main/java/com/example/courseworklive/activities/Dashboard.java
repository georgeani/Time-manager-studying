package com.example.courseworklive.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseworklive.R;
import com.example.courseworklive.adapters.DashboardAdapter;
import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.dialogFragments.DatePickerAdvanced;
import com.example.courseworklive.dialogFragments.IncorrectInputDialog;
import com.example.courseworklive.models.Subject;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class Dashboard extends AppCompatActivity implements DashboardAdapter.OnSubjectDashboardListener,
        DatePickerDialog.OnDateSetListener {

    /**
     * In this activity we implement the Dashboard Adapter Listener
     * and date picker.
     * This is done in order to ensure future extensibility
     * as well as to allow for an easier way to set a date range
     * that the user may want to search in
     * The following are the variables used.
     * They are the UI elements as well as the elements needed to set up
     * the Recycler View
     * */
    private final static String TAG = "Dashboard";
    private ArrayList<Subject> subjects = new ArrayList<>();
    private DashboardAdapter dashboardAdapter;
    private TextView start;
    private Button search;
    private TextView end;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private RecyclerView rvSubjects;
    private TextView emptyView;
    private boolean startOrEnd;
    private BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Setting up the UI elements and their listeners
        start = findViewById(R.id.startDateDashboard);
        end = findViewById(R.id.endDateDashboard);
        search = findViewById(R.id.searchButtonDateDashboard);

        navigation = findViewById(R.id.navigation);
        navigation.setItemIconTintList(null);
        navigation.setOnNavigationItemSelectedListener(navListener);

        rvSubjects = findViewById(R.id.listviewSubjectsDashboard);
        emptyView = findViewById(R.id.empty_view_dashboard);

        //loading the data for the Recycler View
        Thread thread = new Thread() {
            @Override
            public void run() {
                subjects = getSubjects();
            }
        };
        thread.run();

        //checking if data has been found in the Recycler View
        if (subjects.isEmpty()) {
            rvSubjects.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvSubjects.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        dashboardAdapter = new DashboardAdapter(subjects, this);
        rvSubjects.setAdapter(dashboardAdapter);
        // Set layout manager to position the items
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));

        start.setOnClickListener(startListener);
        end.setOnClickListener(endListener);
        search.setOnClickListener(searchListener);

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
                    startActivity(new Intent(Dashboard.this, SubjectsPage.class));
                    break;
                case R.id.main_menu_nav_button:
                    Intent a = new Intent(Dashboard.this, MainActivity.class);
                    startActivity(a);
                    break;
                case R.id.add_subject_nav_menu:
                    Intent b = new Intent(Dashboard.this, AddSubject.class);
                    startActivity(b);
                    break;
            }
            return false;
        }
    };

    /**
     * The listener used to set the starting date of the search
     * */
    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerAdvanced datePicker = new DatePickerAdvanced();
            datePicker.setLimiter(false);
            if (startCalendar != null) {
                datePicker.setmCalender(startCalendar);
            }

            startOrEnd = true;
            datePicker.show(getSupportFragmentManager(), "Start Date");
        }
    };

    /**
     * The listener used to set the ending date of the search
     * */
    private View.OnClickListener endListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerAdvanced datePicker = new DatePickerAdvanced();
            if (endCalendar != null) {
                datePicker.setmCalender(endCalendar);
            }

            if (startCalendar != null) {
                datePicker.setMin(startCalendar);
                datePicker.setLimiter(true);
            }

            startOrEnd = false;
            datePicker.show(getSupportFragmentManager(), "End Date");
        }
    };

    /**
     * The listener used to trigger the
     * search of the data to find the relevant subjects
     * */
    private View.OnClickListener searchListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {

                if (startCalendar.getTimeInMillis() > endCalendar.getTimeInMillis()) {
                    IncorrectInputDialog dialog = new IncorrectInputDialog("Wrong Input", "Dates Inputted are wrong");
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "Incorrect Input");
                } else {

                    subjects.clear();

                    final String SELECTION = "( " + startCalendar.getTimeInMillis() + " <= " + DataContract.Subject_Table.startDate + " AND " + DataContract.Subject_Table.startDate + " <= " + endCalendar.getTimeInMillis()
                            + " ) OR ( " + startCalendar.getTimeInMillis() + " <= " + DataContract.Subject_Table.endDate + " AND " + DataContract.Subject_Table.endDate + " <= " + endCalendar.getTimeInMillis() + " )";


                    Thread th = new Thread() {
                        @Override
                        public void run() {

                            Cursor cursor = getContentResolver().query(DataContract.SUBJECT_URI, null, SELECTION, null, null);
                            assert cursor != null;
                            if (cursor.moveToFirst()) {
                                Subject subjectTemp;
                                while (!cursor.isAfterLast()) {
                                    Calendar calendar = Calendar.getInstance();
                                    subjectTemp = new Subject();
                                    subjectTemp.setSubjectTitle(cursor.getString(cursor.getColumnIndex(DataContract.Subject_Table.name)));
                                    subjectTemp.setWeight(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.weight)));
                                    subjectTemp.setEffort(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.effort)));
                                    calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Subject_Table.startDate)));
                                    subjectTemp.setStartingDate(calendar);
                                    calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Subject_Table.endDate)));
                                    subjectTemp.setEndDate(calendar);
                                    subjectTemp.setTotalHours(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.totalHours)));
                                    subjects.add(subjectTemp);
                                    cursor.moveToNext();
                                }
                            }
                            cursor.close();

                        }
                    };
                    th.run();

                    Log.d(TAG, SELECTION);

                    dashboardAdapter.notifyDataSetChanged();

                }

            } catch (Exception e) {
                IncorrectInputDialog dialog = new IncorrectInputDialog("Incorrect Dates", "Both dates need to be inputted");
                dialog.show(getSupportFragmentManager(), TAG);
            }
        }
    };

    /**
     * Method used to fetch the data of the subjects in order to be put in the recycler view
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
                subjectTemp.setStartingDate(calendar);
                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataContract.Subject_Table.endDate)));
                subjectTemp.setEndDate(calendar);
                subjectTemp.setTotalHours(cursor.getInt(cursor.getColumnIndex(DataContract.Subject_Table.totalHours)));
                subject.add(subjectTemp);
                cursor.moveToNext();
            }
        }
        cursor.close();

        return subject;

    }

    @Override
    public void onItemClick(int item) {
        //Possible extension in case we want to use the touch input in the recycler view
    }

    /**
     * Method used in order to set the parameter dates
     * */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (startOrEnd) {
            startCalendar = Calendar.getInstance();
            startCalendar.set(Calendar.YEAR, year);
            startCalendar.set(Calendar.MONTH, month);
            startCalendar.set(Calendar.DAY_OF_MONTH, day);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            start.setText(format.format(startCalendar.getTime()));
        } else {
            endCalendar = Calendar.getInstance();
            endCalendar.set(Calendar.YEAR, year);
            endCalendar.set(Calendar.MONTH, month);
            endCalendar.set(Calendar.DAY_OF_MONTH, day);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            end.setText(format.format(endCalendar.getTime()));
        }
    }

    /**
     * Removing the listeners from the activity
     * when it is paused
     * */
    @Override
    protected void onPause() {
        navigation.setOnNavigationItemSelectedListener(null);
        start.setOnClickListener(null);
        end.setOnClickListener(null);
        search.setOnClickListener(null);
        super.onPause();

    }

    /**
     * Removing the listeners from the activity
     * when it is destroyed
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigation.setOnNavigationItemSelectedListener(null);
        start.setOnClickListener(null);
        end.setOnClickListener(null);
        search.setOnClickListener(null);
    }

    /**
     * Setting the listeners from the activity
     * when the activity resumes
     * */
    @Override
    protected void onResume() {
        super.onResume();
        subjects = getSubjects();
        dashboardAdapter.notifyDataSetChanged();
        navigation.setOnNavigationItemSelectedListener(navListener);
        start.setOnClickListener(startListener);
        end.setOnClickListener(endListener);
        search.setOnClickListener(searchListener);
    }
}