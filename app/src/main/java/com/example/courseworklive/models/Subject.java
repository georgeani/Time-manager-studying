package com.example.courseworklive.models;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Subject {

    /**
     * The Subject class is a model class, meaning that it keeps the data from the database
     * In this case it has the starting date, ending date, id, title, weight, effort
     * starting and ending dates as well as the total hours spent studying the subject.
     * This is done as it allows for easier handling of data.
     * The following are the variables
     * */

    private int id;
    private String subjectTitle;
    private int weight;
    private int effort;
    private Calendar startingDate;
    private Calendar endDate;
    private int totalHours;

    /**
     * The constructor for theSubject class
     * */

    public Subject() {
    }

    /**
     * Setter and getter methods for the object
     * */
    public static String getDate(Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return format.format(date.getTime());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getEffort() {
        return effort;
    }

    public void setEffort(int effort) {
        this.effort = effort;
    }

    public Calendar getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Calendar startingDate) {
        this.startingDate = startingDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    /**
     * These two methods are used in order to make a map
     * with the data in order to be uploaded to Firebase
     * This one is to update the data in Firebase*/
    public Map<String, Object> getDataForFirestore(){
        Map<String, Object> data = new HashMap<>();
        data.put("deleted",false);
        data.put("subjectTitle", getSubjectTitle());
        data.put("id",getId());
        data.put("weight",getWeight());
        data.put("effort",getEffort());
        data.put("total",getTotalHours());
        data.put("startingDate",getStartingDate().getTimeInMillis());
        data.put("endDate",getEndDate().getTimeInMillis());

        return data;
    }

    /**
     * This one is to add a new subject in Firebase
     * */
    public Map<String, Object> addNewData(){
        Map<String, Object> data = new HashMap<>();
        data.put("subjectTitle", getSubjectTitle());
        //data.put("id",getId());
        data.put("weight",getWeight());
        data.put("effort",getEffort());
        data.put("total",getTotalHours());
        data.put("startingDate",getStartingDate().getTimeInMillis());
        data.put("endDate",getEndDate().getTimeInMillis());
        data.put("deleted",true);

        return data;
    }

    /**
     * This method checks how much progress has been made
     * in studying that subject and it outputs true or false.
     * True meaning that the user has not studied enough
     * */
    public boolean checkProgress(long tomorrow) {
        boolean res = false;

        if (((float) getEndDate().getTimeInMillis() / (float) tomorrow) >= 0.5
                && ((float) getTotalHours() / (float) TimeUnit.MILLISECONDS.convert(getEffort(), TimeUnit.HOURS)) <= 0.5) res = true;

        return res;
    }

}
