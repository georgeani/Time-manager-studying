package com.example.courseworklive.contentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * This class has been created in such a way that helps the Content provider
     * used to access the internal database.
     * It also allows the UpdateFirebase service to perform a data reset
     * by dropping the tables and recreating them.
     * The following are the queries used to build the tables
     * */

    static final String DATABASE_NAME = "SubjectDB";
    static final int DATABASE_VERSION = 2;
    static final String CREATE_DB_TABLE = " CREATE TABLE " + DataContract.Subject_Table.TABLE_NAME
            + " (" + DataContract.Subject_Table.id + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DataContract.Subject_Table.name + " TEXT NOT NULL, "
            + DataContract.Subject_Table.weight + " INTEGER NOT NULL, "
            + DataContract.Subject_Table.effort +" INTEGER NOT NULL, "
            + DataContract.Subject_Table.startDate + " INTEGER NOT NULL, "
            + DataContract.Subject_Table.endDate + " INTEGER NOT NULL, "
            + DataContract.Subject_Table.totalHours + " INTEGER NOT NULL);";

    static final String CREATE_DB_TABLE2 = " CREATE TABLE " + DataContract.Log_Table.TABLE_NAME
            + " (" + DataContract.Log_Table.id  + " INTEGER NOT NULL, "
            + DataContract.Log_Table.date + " INTEGER NOT NULL, "
            + DataContract.Log_Table.length + " INTEGER NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //used to create the tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_TABLE);
        db.execSQL(CREATE_DB_TABLE2);
    }

    //used to upgrade the tables in case the database version is upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.Subject_Table.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.Log_Table.TABLE_NAME);
        onCreate(db);
    }
}