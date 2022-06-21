package com.example.courseworklive.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.courseworklive.R;
import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.dialogFragments.DatePickerAdvanced;
import com.example.courseworklive.dialogFragments.IncorrectInputDialog;
import com.example.courseworklive.models.Subject;
import com.example.courseworklive.service.UpdateFirebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AddSubject extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    /**
     * The variables used to keep the elements present in the MainActivity UI
     * TAG is used for logs
     * DatePicker is being implemented in order to ensure an easier input of the starting and
     * ending dates.
     * Furthermore, SharedPreferences are used in order to ensure that in case the user forgets to
     * save the data and puts the data in the background the data is not lost
     * This activity is also used in case a user wants to modify an existing
     * subject
     * */

    private static final String TAG = "Save Subject";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText subject;
    private EditText weight;
    private EditText effort;
    private TextView start;
    private TextView end;
    private ImageButton imageButton;
    private boolean noSave = false;
    private boolean startOrEnd = true;
    private Bundle extras;
    private Calendar startCal;
    private Calendar endCal;

    /**
     * The button listener used to save the data in the content provider
     * as well as to start the backing up procedure in case they are logged in with the Firebase
     * */
    private View.OnClickListener saveButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //checking whether or not we are adding a new subject or modifying an existing one
            if (sharedPreferences.getBoolean("bundle", false)) {
                Toast.makeText(getApplicationContext(), "Subject Updated", Toast.LENGTH_SHORT).show();

                try {
                    if (!subject.getText().toString().isEmpty() || !weight.getText().toString().isEmpty() ||
                            !effort.getText().toString().isEmpty() || endCal != null || startCal != null) {
                        if (startCal.getTimeInMillis() < endCal.getTimeInMillis()) {

                            //updating an existing subject
                            cleanup();
                            startActivity(new Intent(AddSubject.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            ContentValues values = new ContentValues();
                            values.put(DataContract.Subject_Table.name, subject.getText().toString());
                            values.put(DataContract.Subject_Table.weight, weight.getText().toString());
                            values.put(DataContract.Subject_Table.effort, effort.getText().toString());
                            values.put(DataContract.Subject_Table.startDate, startCal.getTimeInMillis());
                            //Log.d(TAG, startCal.getTimeInMillis() + "");
                            //Log.d(TAG, "" + endCal.getTimeInMillis());
                            values.put(DataContract.Subject_Table.endDate, endCal.getTimeInMillis());
                            values.put(DataContract.Subject_Table.totalHours, Long.parseLong(Objects.requireNonNull(extras.getString("total"))));
                            getContentResolver().update(DataContract.SUBJECT_URI,
                                    values, DataContract.Subject_Table.id + "=?", new String[]{String.valueOf(extras.getInt("id"))});

                            SharedPreferences sh = getSharedPreferences("loginInfo", MODE_PRIVATE);
                            if (!Objects.requireNonNull(sh.getString("username", "")).isEmpty()){
                                updateData(extras.getInt("id"), extras.getString("total"));
                            }
                            finish();

                        } else {
                            //in case incorrect dates are inputted the user is notified
                            //through the use of a special made dialog view
                            IncorrectInputDialog dialog = new IncorrectInputDialog("Incorrect Dates", "Dates Inputted are incorrect");
                            dialog.setCancelable(false);
                            dialog.show(getSupportFragmentManager(), "Wrong Dates");
                        }
                    } else {
                        //in case parts of the input are missing
                        IncorrectInputDialog dialog = new IncorrectInputDialog("Incomplete Input", "Not all fields have been completed");
                        dialog.setCancelable(false);
                        dialog.show(getSupportFragmentManager(), "Wrong Input");
                    }
                } catch (Exception e) {
                    IncorrectInputDialog dialog = new IncorrectInputDialog("Incomplete Input", "Not all fields have been completed");
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "Wrong Input");
                }


            } else {

                //Adding a new subject to the content provider and firebase

                try {
                    if (!subject.getText().toString().isEmpty() || !weight.getText().toString().isEmpty() ||
                            !effort.getText().toString().isEmpty() || endCal != null || startCal != null) {
                        if (startCal.getTimeInMillis() < endCal.getTimeInMillis()) {
                            //cleaning the shared preferences file
                            cleanup();
                            startActivity(new Intent(AddSubject.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            ContentValues values = new ContentValues();
                            values.put(DataContract.Subject_Table.name, subject.getText().toString());
                            values.put(DataContract.Subject_Table.weight, weight.getText().toString());
                            values.put(DataContract.Subject_Table.effort, effort.getText().toString());
                            values.put(DataContract.Subject_Table.startDate, startCal.getTimeInMillis());
                            //Log.d(TAG, startCal.getTimeInMillis() + "");
                            //Log.d(TAG, "" + endCal.getTimeInMillis());
                            values.put(DataContract.Subject_Table.endDate, endCal.getTimeInMillis());
                            values.put(DataContract.Subject_Table.totalHours, 0);
                            getContentResolver().insert(DataContract.SUBJECT_URI, values);
                            Toast.makeText(getApplicationContext(), "Subject Added", Toast.LENGTH_SHORT).show();
                            SharedPreferences sh = getSharedPreferences("loginInfo", MODE_PRIVATE);
                            if (!Objects.requireNonNull(sh.getString("username", "")).isEmpty()){
                                addNewSubject();
                            }
                            addNewSubject();
                            finish();

                        } else {
                            //in case incorrect dates are inputted the user is notified
                            //through the use of a special made dialog view
                            IncorrectInputDialog dialog = new IncorrectInputDialog("Incorrect Dates", "Dates Inputted are incorrect");
                            dialog.setCancelable(false);
                            dialog.show(getSupportFragmentManager(), "Wrong Dates");
                        }
                    } else {
                        //in case parts of the input are missing
                        IncorrectInputDialog dialog = new IncorrectInputDialog("Incomplete Input", "Not all fields have been completed");
                        dialog.setCancelable(false);
                        dialog.show(getSupportFragmentManager(), "Wrong Input");
                    }
                } catch (Exception e) {
                    IncorrectInputDialog dialog = new IncorrectInputDialog("Incomplete Input", "Not all fields have been completed");
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "Wrong Input");
                }

            }

        }
    };

    /**
     * The onClickListener used to start the date picker that will select the
     * starting date
     * */
    private View.OnClickListener startDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            com.example.courseworklive.dialogFragments.DatePicker datePicker;
            datePicker = new com.example.courseworklive.dialogFragments.DatePicker();

            //making sure that the date picker cannot pick any future dates
            Calendar test = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            //updating the textview to reflect the selected date
            try {
                test.setTime(Objects.requireNonNull(simpleDateFormat.parse((String) start.getText())));
                datePicker.setmCalender(test);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startOrEnd = true;
            datePicker.show(getSupportFragmentManager(), "Start Date");
        }
    };

    /**
     * The onClickListener used to start the date picker that will select the
     * ending date
     * */
    private View.OnClickListener endDate = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            DatePickerAdvanced datePicker;
            datePicker = new DatePickerAdvanced();

            //restricting the datepicker to dates after the one that is today

            Calendar test = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            //updating the textview to reflect the selected date
            try {
                test.setTime(Objects.requireNonNull(simpleDateFormat.parse((String) end.getText())));
                datePicker.setmCalender(test);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                test.setTime(Objects.requireNonNull(simpleDateFormat.parse((String) start.getText())));
                test.setTimeInMillis(test.getTimeInMillis() + TimeUnit.DAYS.toMillis(1));
                datePicker.setMin(test);
            } catch (Exception e) {
                e.printStackTrace();
            }

            startOrEnd = false;
            datePicker.setLimiter(true);
            datePicker.show(getSupportFragmentManager(), "End Date");

        }
    };

    /**
     * onCreate method: used to instantiate the activity
     * makes sure the listeners are attached to their respected UI element
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        sharedPreferences = getSharedPreferences("SubjectPreSave", MODE_PRIVATE);

        final ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        subject = findViewById(R.id.add_subject_page_title_editbox);
        weight = findViewById(R.id.add_subject_page_weight_editbox);
        effort = findViewById(R.id.add_subject_page_effort_editbox);
        start = findViewById(R.id.add_subject_page_start_editbox);
        end = findViewById(R.id.add_subject_page_end_editbox);

        //if shared preferences are written, it then loads
        //the values to the UI
        //this is done in order for the user to see all the available info
        if (sharedPreferences.getBoolean("saved", true) && extras == null) {
            subject.setText(sharedPreferences.getString("subject", ""));
            weight.setText(sharedPreferences.getString("weight", ""));
            effort.setText(sharedPreferences.getString("effort", ""));
            start.setText(sharedPreferences.getString("start", ""));
            end.setText(sharedPreferences.getString("end", ""));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                startCal.setTime(Objects.requireNonNull(simpleDateFormat.parse((String) start.getText())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                endCal.setTime(Objects.requireNonNull(simpleDateFormat.parse((String) end.getText())));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (sharedPreferences.getBoolean("bundle", true)) {
                TextView titleOfPage = findViewById(R.id.add_subject_page_title);
                titleOfPage.setText("Change Subject");
            }
        }

        //checking if a subject needs to be modified
        //if so the title of the page changes
        extras = getIntent().getExtras();
        if (extras != null) {
            TextView titleOfPage = findViewById(R.id.add_subject_page_title);
            titleOfPage.setText("Change Subject");
            subject.setText(extras.getString("title"));
            weight.setText(extras.getString("weight"));
            effort.setText(extras.getString("effort"));
            startCal = Calendar.getInstance();
            startCal.setTimeInMillis(extras.getLong("start"));
            start.setText(Subject.getDate(startCal));
            endCal = Calendar.getInstance();
            endCal.setTimeInMillis(extras.getLong("end"));
            end.setText(Subject.getDate(endCal));
        }

        //setting the listeners up
        imageButton = findViewById(R.id.save_button);
        imageButton.setOnClickListener(saveButtonListener);
        start.setOnClickListener(startDate);
        end.setOnClickListener(endDate);

    }

    /**
     * Method used to update the subject in Firebase
     * it takes as input the id and total time spend studying
     * and updates Firebase
     * */
    private void updateData(int id, String total) {
        //Log.d(TAG, extras.getString("title") + " title");
        String temp = extras.getString("title");

        SharedPreferences sh = getSharedPreferences("loginInfo", MODE_PRIVATE);
        assert temp != null;
        //checking to see if a subject has its title changed
        //if so the previous one needs to have a variable updated in order to ensure it is
        //not downloaded by mistake in the future
        if (!temp.equals(subject.getText().toString()) && sh.getString("username", "").isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("deleted", true);

            FirebaseFirestore.getInstance().setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .build());

            FirebaseFirestore.getInstance().collection(sh.getString("username", "")
                    + "Subjects").document(temp).update(data);
        }

        Bundle bundle = new Bundle();
        bundle.putInt("flag", 1);
        bundle.putInt("id", id);
        bundle.putString("title", subject.getText().toString());
        bundle.putString("weight", weight.getText().toString());
        bundle.putString("effort", effort.getText().toString());
        bundle.putLong("start", startCal.getTimeInMillis());
        bundle.putLong("end", endCal.getTimeInMillis());
        bundle.putString("total", total);
        Log.d(TAG, "onClick: update");
        startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(bundle));
    }

    /**
     * adding a new subject to Firebase
     * */
    private void addNewSubject() {
        Bundle bundle = new Bundle();
        bundle.putInt("flag", 5);
        bundle.putString("title", subject.getText().toString());
        bundle.putString("weight", weight.getText().toString());
        bundle.putString("effort", effort.getText().toString());
        bundle.putLong("start", startCal.getTimeInMillis());
        bundle.putLong("end", endCal.getTimeInMillis());
        bundle.putString("total", "0");
        Log.d(TAG, "onClick: update");
        startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(bundle));
    }

    /**
     * Removing the listeners when the activity is to be destroyed
     * */
    @Override
    protected void onDestroy() {
        imageButton.setOnClickListener(null);
        start.setOnClickListener(null);
        end.setOnClickListener(null);
        super.onDestroy();
    }

    /**
     * Removing the listeners when the activity is to be paused
     * Updating the shared preference
     * Even removing the data if they do not need to be persisted
     * */
    @Override
    protected void onPause() {
        imageButton.setOnClickListener(null);
        start.setOnClickListener(null);
        end.setOnClickListener(null);
        if (!noSave) {
            sharedPreferences = getSharedPreferences("SubjectPreSave", MODE_PRIVATE);
            editor = sharedPreferences.edit();
            editor.putBoolean("saved", true);
            editor.putString("subject", subject.getText().toString());
            editor.putString("weight", weight.getText().toString());
            editor.putString("effort", effort.getText().toString());
            editor.putString("start", start.getText().toString());
            editor.putString("end", end.getText().toString());
            if (extras != null) editor.putBoolean("bundle", true);
            else editor.putBoolean("bundle", false);
            editor.apply();
        }
        super.onPause();
    }

    /**
     * Resetting the listeners when the activity resumses
     * as well as the data that has persisted
     * */
    @Override
    protected void onResume() {
        imageButton.setOnClickListener(saveButtonListener);
        start.setOnClickListener(startDate);
        end.setOnClickListener(endDate);
        sharedPreferences = getSharedPreferences("SubjectPreSave", MODE_PRIVATE);
        subject.setText(sharedPreferences.getString("subject", ""));
        weight.setText(sharedPreferences.getString("weight", ""));
        effort.setText(sharedPreferences.getString("effort", ""));
        start.setText(sharedPreferences.getString("start", ""));
        end.setText(sharedPreferences.getString("end", ""));
        if (extras != null || sharedPreferences.getBoolean("bundle", true)) {
            TextView titleOfPage = findViewById(R.id.add_subject_page_title);
            titleOfPage.setText("Change Subject");
        }
        super.onResume();
    }

    /**
     * Removing the listeners when the activity is to go back
     * Even removing the data if they do not need to be persisted
     * */
    @Override
    public void onBackPressed() {
        imageButton.setOnClickListener(null);
        start.setOnClickListener(null);
        end.setOnClickListener(null);
        cleanup();
        super.onBackPressed();
    }

    /**
     * Removing the listeners when the activity is to go back through the action bar
     * Even removing the data if they do not need to be persisted
     * */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            cleanup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method used to remove all the data from persisting
     * in the UI
     * */
    private void cleanup() {
        noSave = true;
        sharedPreferences = getSharedPreferences("SubjectPreSave", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.putBoolean("saved", false);
        editor.putBoolean("bundle", false);
        editor.apply();
    }

    /**
     * Method used in order to set the dates
     * allows for both dates to be set
     * */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        if (startOrEnd) {
            startCal = Calendar.getInstance();
            startCal.set(Calendar.YEAR, year);
            startCal.set(Calendar.MONTH, month);
            startCal.set(Calendar.DAY_OF_MONTH, day);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            start.setText(format.format(startCal.getTime()));
        } else {
            endCal = Calendar.getInstance();
            endCal.set(Calendar.YEAR, year);
            endCal.set(Calendar.MONTH, month);
            endCal.set(Calendar.DAY_OF_MONTH, day);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            end.setText(format.format(endCal.getTime()));
        }
    }

}