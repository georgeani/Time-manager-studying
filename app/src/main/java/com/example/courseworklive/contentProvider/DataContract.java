package com.example.courseworklive.contentProvider;

import android.net.Uri;
import android.provider.BaseColumns;

public class DataContract {

    /**
     * This class mainly contains the values needed to access the content provider
     * as well as the columns for the database tables
     * */

    private DataContract(){}

    /**
     * The variables used to access the provider
     * That includes the provider name, subject path as
     * well as the URIs of both tables */
    public static final String PROVIDER_NAME = "com.example.contentprovider.MyContentProvider";
    public static final String SUBJECT_PATH = "subjects";
    public static final String LOG_PATH = "logs";
    public static final String URL = "content://" + PROVIDER_NAME + "/subjects";
    public static final Uri SUBJECT_URI = Uri.parse(URL);
    public static final Uri LOGS_URI = Uri.parse("content://" + PROVIDER_NAME + "/logs");

    /**
     * The columns for the subject table
     * */
    public static class Subject_Table implements BaseColumns{
        public static final String TABLE_NAME = "Subjects";
        public static final String id = "id";
        public static final String name = "name";
        public static final String weight = "weight";
        public static final String effort = "effort";
        public static final String startDate = "startDate";
        public static final String endDate = "endDate";
        public static final String totalHours = "total";
    }

    /**
     * The columns for the log table
     * */
    public static class Log_Table implements BaseColumns{
        public static final String TABLE_NAME = "Logs";
        public static final String id = "id";
        public static final String date = "date";
        public static final String length = "length";
    }



}
