package com.example.courseworklive.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.courseworklive.R;
import com.example.courseworklive.contentProvider.DataContract;
import com.example.courseworklive.dialogFragments.CheckForDeletion;
import com.example.courseworklive.dialogFragments.IncorrectInputDialog;
import com.example.courseworklive.service.UpdateFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginToFirebase extends AppCompatActivity implements CheckForDeletion.CheckForDeletionListener {

    /**
     * In this activity we implement the CheckForDeletion Listener
     * This is done in order to allow for the user to select whether they want to
     * have their data overriden in case of a system reset or not.
     * The following are the variables used.
     * */

    private SharedPreferences shared;
    private EditText username;
    private EditText password;
    private Button reset;
    private Button logOut;
    private Button login;
    private FirebaseFirestore firestore;
    private boolean resetData = false;

    /**
     * The onCreate method used to instantiate the objects and set up the listeners
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_to_firebase);

        //setting up the object and their listeners
        shared = getSharedPreferences("loginInfo", MODE_PRIVATE);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password_login);
        reset = findViewById(R.id.reset_button);
        reset.setOnClickListener(resetDataListener);
        login = findViewById(R.id.login_button);
        login.setOnClickListener(loginListener);
        logOut = findViewById(R.id.logout_button);
        logOut.setOnClickListener(logOutListener);

        firestore = FirebaseFirestore.getInstance();

        //checking whether the user has previously logged in
        //if so certain services are restricted
        String usernameSaved = shared.getString("username","");
        String passwordSaved = shared.getString("password","");
        assert usernameSaved != null;
        assert passwordSaved != null;
        if (usernameSaved.isEmpty() || passwordSaved.isEmpty()){
            reset.setClickable(false);
            logOut.setClickable(false);
        } else {
            login.setClickable(false);
            username.setClickable(false);
            username.setText(usernameSaved);
            password.setClickable(false);
            password.setText(passwordSaved);
            username.setFocusable(false);
            username.setFocusableInTouchMode(false);
            password.setFocusable(false);
            password.setFocusableInTouchMode(false);
        }

        //setting up the action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Removing the listeners from the activity
     * when it is paused
     * */
    @Override
    protected void onPause() {
        reset.setOnClickListener(null);
        login.setOnClickListener(null);
        logOut.setOnClickListener(null);
        super.onPause();
    }

    /**
     * Removing the listeners from the activity
     * when it is destroyed
     * */
    @Override
    protected void onDestroy() {
        reset.setOnClickListener(null);
        login.setOnClickListener(null);
        logOut.setOnClickListener(null);
        super.onDestroy();
    }

    /**
     * Setting up the listeners in the activity
     * when it is resumed
     * */
    @Override
    protected void onResume() {
        super.onResume();
        reset.setOnClickListener(resetDataListener);
        login.setOnClickListener(loginListener);
        logOut.setOnClickListener(logOutListener);
    }

    /**
     * Removing the listeners from the activity
     * when the back button is pressed as well as destroying it
     * */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        reset.setOnClickListener(null);
        login.setOnClickListener(null);
        logOut.setOnClickListener(null);
        finish();
    }

    /**
     * Removing the listeners from the activity
     * when the back button in the actionbar is pressed
     * is pressed as well as destroying it
     * */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            reset.setOnClickListener(null);
            login.setOnClickListener(null);
            logOut.setOnClickListener(null);
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Login functionality listener
     * it is triggered when the Login button is pressed
     * It starts the login process*/
    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("Login","Runs");
            //starting the login process
            useFirebase();
        }
    };

    /**
     * The method used to begin the login process to Firebase
     * This is in order to check if the credentials are saved in Firebase
     * as well as to do a hardware reset in case that data already exists
     * */
    private void useFirebase(){
        //querying to see whether the username and password match anyone in the database
        CollectionReference reference = firestore.collection("users");
        Log.d("Login","Runs2");
        Log.d("Login", username.getText().toString());
        String use = username.getText().toString();
        Log.d("Login", password.getText().toString());
        String pass = password.getText().toString();
        Query query = reference.whereEqualTo("username", use).whereEqualTo("password", pass);
        Log.d("Login","Runs3");
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    //in case the query has results it means that the user exits
                    if (Objects.requireNonNull(task.getResult()).size() > 0){
                        Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor edit = shared.edit();
                        edit.clear();
                        Log.d("Login", username.getText().toString() + " username");
                        Log.d("Login", password.getText().toString() + " pass");
                        edit.putString("username", username.getText().toString());
                        edit.putString("password", password.getText().toString());
                        edit.apply();

                        //checking if data exists and a hardware reset is needed
                        if (checkDataAlreadyExists()){
                            CheckForDeletion deletion = new CheckForDeletion("Data Reset","Do you want to reset all your local data?", "Yes", "No");
                            deletion.setCancelable(false);
                            deletion.show(getSupportFragmentManager(),"Deletion");
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putInt("flag", 2);
                            Log.d("Login", "Run Service");
                            startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(bundle));
                            login.setClickable(false);
                        }
                    } else {
                        //informing the user that the login failed
                        IncorrectInputDialog dialog = new IncorrectInputDialog("Wrong Credential","Your password or username is wrong");
                        dialog.setCancelable(false);
                        dialog.show(getSupportFragmentManager(), "Wrong Input");
                        Log.d("Login", "Didnt run");
                    }

                    //Log.d("Login", "Query run");

                } else {
                    //informing the user that the login failed
                    IncorrectInputDialog dialog = new IncorrectInputDialog("Wrong Credential","Your password or username is wrong");
                    dialog.setCancelable(false);
                    dialog.show(getSupportFragmentManager(), "Wrong Input");
                    Log.d("Login", "Didnt run");
                }
            }
        });
    }

    /**
     * Method that checks whether the application
     * has data already pre-saved before the use of Firebase
     * */
    private boolean checkDataAlreadyExists(){
        Cursor cursor = getContentResolver().query(DataContract.SUBJECT_URI, null, null, null, null);
        assert cursor != null;
        boolean temp = cursor.getCount() > 0;
        cursor.close();
        return temp;
    }

    /**
     * In case the positive click is selected
     * UpdateFirebase service is started in order to update the data from Firebase
     * */
    @Override
    public void onPositiveClick(DialogFragment dialogFragment) {
        Bundle bundle = new Bundle();
        bundle.putInt("flag", 2);
        startService(new Intent(getApplicationContext(), UpdateFirebase.class).putExtras(bundle));
        login.setClickable(false);
        reset.setClickable(true);
        logOut.setClickable(true);
        username.setFocusable(false);
        username.setFocusableInTouchMode(false);
        password.setFocusable(false);
        password.setFocusableInTouchMode(false);
    }

    /**
     * In case the user chooses to not reset the data then in the case of login
     * The login credentials are deleted
     * else nothing happens
     * */
    @Override
    public void onNegativeClick(DialogFragment dialogFragment) {
        if (!resetData){
            SharedPreferences.Editor edit = shared.edit();
            edit.clear();
            edit.apply();
            username.setText("");
            password.setText("");
        } else {
            resetData = false;
        }
    }

    /**
     * The listener used for the logout process
     * it changes the clickeable buttons as well as clearing the credentials
     * and the EditText Fields
     * */
    private View.OnClickListener logOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences.Editor editor = shared.edit();
            editor.clear();
            editor.apply();
            username.setText("");
            password.setText("");
            username.setFocusable(true);
            username.setFocusableInTouchMode(true);
            password.setFocusable(true);
            password.setFocusableInTouchMode(true);
            login.setClickable(true);
            reset.setClickable(false);
            logOut.setClickable(false);
        }
    };

    /**
     * The listener used for the reset Data process
     * it prompts the user to check whether the data reset process should go ahead
     * */
    private View.OnClickListener resetDataListener = new View.OnClickListener() {
            @Override
        public void onClick(View view) {
            resetData = true;
            CheckForDeletion deletion = new CheckForDeletion("Data Reset","Do you want to reset all your local data?", "Yes", "No");
            deletion.setCancelable(false);
            deletion.show(getSupportFragmentManager(),"Deletion");

        }
    };

}