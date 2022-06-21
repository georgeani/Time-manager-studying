package com.example.courseworklive.models;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Logs {

    /**
     * The Logs class is a model class, meaning that it keeps the data from the database
     * In this case it is a log of the date,
     * the length of the recording and the corresponding subject.
     * This is done as it allows for easier handling of data.
     * The following are the variables
     * */

    private int id;
    private Calendar date;
    private int timeSpent;

    //the constructor
    public Logs(){}

    /**
     * Setter and getter methods for the object
     * */
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Calendar getDate() {
        return date;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    //returns the date in a string format
    public static String getDate(Calendar date){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return format.format(date.getTime());
    }

    //returns the time from milliseconds to hours and minutes spent
    public static String getTimeSpentHours(long time){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TimeUnit.MILLISECONDS.toHours(time));
        stringBuilder.append(":");
        int temp = (int) TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(time)));
        if (temp <10){
            stringBuilder.append(0);
        }
        stringBuilder.append(temp);
        return String.valueOf(stringBuilder);
    }
    //returns the time from milliseconds to hours, minutes and seconds spent spent
    public static String getTimeSpentMins(long time){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TimeUnit.MILLISECONDS.toHours(time));
        stringBuilder.append(":");
        int temp = (int) TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(time)));
        if (temp <10){
            stringBuilder.append(0);
        }
        stringBuilder.append(temp);
        stringBuilder.append(":");
        temp = (int) TimeUnit.MILLISECONDS.toSeconds(time - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(time)) - TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(time - TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(time)))));
        Log.d("Log", String.valueOf(temp));
        if (temp <10){
            stringBuilder.append(0);
        }
        stringBuilder.append(temp);
        return String.valueOf(stringBuilder);
    }

}
