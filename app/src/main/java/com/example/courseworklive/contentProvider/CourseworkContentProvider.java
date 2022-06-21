package com.example.courseworklive.contentProvider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.HashMap;

import static com.example.courseworklive.contentProvider.DataContract.LOGS_URI;
import static com.example.courseworklive.contentProvider.DataContract.LOG_PATH;
import static com.example.courseworklive.contentProvider.DataContract.PROVIDER_NAME;
import static com.example.courseworklive.contentProvider.DataContract.SUBJECT_PATH;
import static com.example.courseworklive.contentProvider.DataContract.SUBJECT_URI;

public class CourseworkContentProvider extends ContentProvider {

    /**
     * The Content Provider is created in order to ensure that data from the applications
     * database is accessible from outside the application
     * You can see all the Uri setup here as well as the matcher
     * */
    private SQLiteDatabase db;

    public static final int uriCodeSubjects =1;
    public static final int uriCodeLogs = 2;
    static final UriMatcher uriMatcher;
    private static HashMap<String, String> values;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, SUBJECT_PATH, uriCodeSubjects);
        uriMatcher.addURI(PROVIDER_NAME, SUBJECT_PATH + "/*", uriCodeSubjects);
        uriMatcher.addURI(PROVIDER_NAME, LOG_PATH, uriCodeLogs);
        uriMatcher.addURI(PROVIDER_NAME,  LOG_PATH+ "/*", uriCodeLogs);
    }

    public CourseworkContentProvider() {
    }

    //deleting content from the database
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int count = 0;
        switch (uriMatcher.match(uri)) {
            case uriCodeSubjects:
                count = db.delete(DataContract.Subject_Table.TABLE_NAME, selection, selectionArgs);
                break;
            case uriCodeLogs:
                count = db.delete(DataContract.Log_Table.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }

    @Override
    public String getType(Uri uri) {

        switch (uriMatcher.match(uri)){
            case uriCodeSubjects:
                return "vnd.android.cursor.dir/subjects";
            case uriCodeLogs:
                return "vnd.android.cursor.dir/logs";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

    }

    //inserting data in the database
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        long rowID = -1 ;
        Uri _uri = null;
        switch (uriMatcher.match(uri)){
            case uriCodeSubjects:
                rowID = db.insert(DataContract.Subject_Table.TABLE_NAME, "", values);
                _uri = ContentUris.withAppendedId(SUBJECT_URI, rowID);
                break;
            case uriCodeLogs:
                rowID = db.insert(DataContract.Log_Table.TABLE_NAME, "", values);
                _uri = ContentUris.withAppendedId(LOGS_URI, rowID);
                break;
            default:
                break;
        }

        if (rowID > 0) {
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLiteException("Failed to add a record into " + uri);

    }

    //setting up the database connection
    @Override
    public boolean onCreate() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        db = databaseHelper.getWritableDatabase();
        return db != null;
    }

    //getting results from the database
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();


        switch (uriMatcher.match(uri)) {
            case uriCodeSubjects:
                qb.setProjectionMap(values);
                qb.setTables(DataContract.Subject_Table.TABLE_NAME);
                break;
            case uriCodeLogs:
                qb.setProjectionMap(values);
                qb.setTables(DataContract.Log_Table.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (sortOrder == null || sortOrder.equals("")) {
            sortOrder = "id";
        }
        Cursor c = qb.query(db, projection, selection, selectionArgs, null,
                null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;

    }

    //updating the database
    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int count;
        switch (uriMatcher.match(uri)) {
            case uriCodeSubjects:
                count = db.update(DataContract.Subject_Table.TABLE_NAME, values, selection, selectionArgs);
                break;
            case uriCodeLogs:
                count = db.update(DataContract.Log_Table.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;

    }


}
